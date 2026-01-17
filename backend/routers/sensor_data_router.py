"""
Event Sensor Data Router
API endpoints for retrieving sensor data from events
"""

from fastapi import APIRouter, Depends, HTTPException
from typing import List, Optional
from datetime import datetime, timedelta
from database import get_db_connection

router = APIRouter(prefix="/sensor_data", tags=["sensor_data"])


@router.get("/event/{event_id}")
async def get_event_sensor_data(
    event_id: int,
    sensor_type: Optional[str] = None,
    limit: int = 100
):
    """Get sensor data for a specific event"""
    conn = get_db_connection()
    cursor = conn.cursor()
    
    # Check if event exists
    cursor.execute("SELECT id FROM events WHERE id = ?", (event_id,))
    event = cursor.fetchone()
    
    if not event:
        conn.close()
        raise HTTPException(status_code=404, detail="Event not found")
    
    # Build query based on sensor_type filter
    if sensor_type:
        query = """
            SELECT id, event_fk, sensor_type, value, timestamp
            FROM event_sensor_data
            WHERE event_fk = ? AND sensor_type = ?
            ORDER BY timestamp DESC
            LIMIT ?
        """
        cursor.execute(query, (event_id, sensor_type, limit))
    else:
        query = """
            SELECT id, event_fk, sensor_type, value, timestamp
            FROM event_sensor_data
            WHERE event_fk = ?
            ORDER BY timestamp DESC
            LIMIT ?
        """
        cursor.execute(query, (event_id, limit))
    
    rows = cursor.fetchall()
    conn.close()
    
    return {
        "event_id": event_id,
        "count": len(rows),
        "data": [dict(row) for row in rows]
    }


@router.get("/event/{event_id}/latest")
async def get_latest_sensor_readings(
    event_id: int
):
    """Get the latest reading for each sensor type for an event"""
    conn = get_db_connection()
    cursor = conn.cursor()
    
    # Check if event exists
    cursor.execute("SELECT id FROM events WHERE id = ?", (event_id,))
    event = cursor.fetchone()
    
    if not event:
        conn.close()
        raise HTTPException(status_code=404, detail="Event not found")
    
    # Get latest reading for each sensor type
    cursor.execute(
        """
        SELECT esd.sensor_type, esd.value, esd.timestamp
        FROM event_sensor_data esd
        INNER JOIN (
            SELECT sensor_type, MAX(timestamp) as max_timestamp
            FROM event_sensor_data
            WHERE event_fk = ?
            GROUP BY sensor_type
        ) latest ON esd.sensor_type = latest.sensor_type 
            AND esd.timestamp = latest.max_timestamp
        WHERE esd.event_fk = ?
        """,
        (event_id, event_id)
    )
    
    rows = cursor.fetchall()
    conn.close()
    
    # Convert to dictionary format
    latest_readings = {row["sensor_type"]: {
        "value": row["value"],
        "timestamp": row["timestamp"]
    } for row in rows}
    
    return {
        "event_id": event_id,
        "latest_readings": latest_readings
    }


@router.get("/event/{event_id}/stats")
async def get_sensor_stats(
    event_id: int,
    sensor_type: str,
    hours: int = 24
):
    """Get statistics for a specific sensor type over a time period"""
    conn = get_db_connection()
    cursor = conn.cursor()
    
    # Check if event exists
    cursor.execute("SELECT id FROM events WHERE id = ?", (event_id,))
    event = cursor.fetchone()
    
    if not event:
        conn.close()
        raise HTTPException(status_code=404, detail="Event not found")
    
    # Calculate time threshold
    threshold_time = (datetime.now() - timedelta(hours=hours)).isoformat()
    
    # Get all readings for the sensor type within the time period
    cursor.execute(
        """
        SELECT value, timestamp
        FROM event_sensor_data
        WHERE event_fk = ? AND sensor_type = ? AND timestamp >= ?
        ORDER BY timestamp DESC
        """,
        (event_id, sensor_type, threshold_time)
    )
    
    rows = cursor.fetchall()
    conn.close()
    
    if not rows:
        return {
            "event_id": event_id,
            "sensor_type": sensor_type,
            "hours": hours,
            "count": 0,
            "data": []
        }
    
    # Try to calculate numeric statistics
    try:
        values = [float(row["value"]) for row in rows]
        stats = {
            "min": min(values),
            "max": max(values),
            "avg": sum(values) / len(values),
            "count": len(values)
        }
    except (ValueError, TypeError):
        # If values aren't numeric, just return the data points
        stats = {
            "count": len(rows)
        }
    
    return {
        "event_id": event_id,
        "sensor_type": sensor_type,
        "hours": hours,
        "stats": stats,
        "data": [dict(row) for row in rows]
    }
