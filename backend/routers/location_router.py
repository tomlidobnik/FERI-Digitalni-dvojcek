from fastapi import APIRouter, HTTPException, status, Depends
from pydantic import BaseModel
from typing import Optional, List
from database import get_db_connection
from routers.user_router import get_authenticated_user
import json

router = APIRouter(prefix="/api/location", tags=["location"])


# Pydantic models
class LocationOutlineCreate(BaseModel):
    points: str


class LocationOutlineResponse(BaseModel):
    id: Optional[int]
    points: str


class LocationCreate(BaseModel):
    info: Optional[str] = None
    longitude: Optional[float] = None
    latitude: Optional[float] = None
    location_outline_fk: Optional[int] = None


class LocationResponse(BaseModel):
    id: Optional[int]
    info: Optional[str]
    longitude: Optional[float]
    latitude: Optional[float]
    location_outline_fk: Optional[int]


class LocationWithOutline(BaseModel):
    id: Optional[int]
    info: Optional[str]
    longitude: Optional[float]
    latitude: Optional[float]
    location_outline: Optional[LocationOutlineResponse]


@router.post("/create", status_code=status.HTTP_201_CREATED)
def create_location(location_data: LocationCreate):
    """Create a new location."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        cursor.execute(
            """
            INSERT INTO locations (info, longitude, latitude, location_outline_fk)
            VALUES (?, ?, ?, ?)
        """,
            (
                location_data.info,
                location_data.longitude,
                location_data.latitude,
                location_data.location_outline_fk,
            ),
        )
        conn.commit()
        conn.close()
        return {"status": "created"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to create location",
        )


@router.put("/update")
def update_location(location_id: int, location_data: LocationCreate):
    """Update a location."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT id FROM locations WHERE id = ?", (location_id,))
    if not cursor.fetchone():
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Location not found"
        )

    try:
        cursor.execute(
            """
            UPDATE locations
            SET info = ?, longitude = ?, latitude = ?, location_outline_fk = ?
            WHERE id = ?
        """,
            (
                location_data.info,
                location_data.longitude,
                location_data.latitude,
                location_data.location_outline_fk,
                location_id,
            ),
        )
        conn.commit()
        conn.close()
        return {"status": "updated"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to update location",
        )


@router.get("/all", response_model=List[LocationResponse])
def get_all_locations():
    """Get all locations."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute(
        "SELECT id, info, longitude, latitude, location_outline_fk FROM locations"
    )
    rows = cursor.fetchall()
    conn.close()

    return [
        {
            "id": row["id"],
            "info": row["info"],
            "longitude": row["longitude"],
            "latitude": row["latitude"],
            "location_outline_fk": row["location_outline_fk"],
        }
        for row in rows
    ]


@router.get("/by_id/{location_id}", response_model=LocationResponse)
def get_location_by_id(location_id: int):
    """Get location by ID."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute(
        "SELECT id, info, longitude, latitude, location_outline_fk FROM locations WHERE id = ?",
        (location_id,),
    )
    row = cursor.fetchone()
    conn.close()

    if not row:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Location not found"
        )

    return {
        "id": row["id"],
        "info": row["info"],
        "longitude": row["longitude"],
        "latitude": row["latitude"],
        "location_outline_fk": row["location_outline_fk"],
    }


@router.delete("/delete/{location_id}")
def delete_location(location_id: int):
    """Delete a location."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT id FROM locations WHERE id = ?", (location_id,))
    if not cursor.fetchone():
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Location not found"
        )

    try:
        cursor.execute("DELETE FROM locations WHERE id = ?", (location_id,))
        conn.commit()
        conn.close()
        return {"status": "deleted"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to delete location",
        )


@router.get("/list", response_model=List[LocationWithOutline])
def get_all_locations_with_outline():
    """Get all locations with their outlines."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute(
        "SELECT id, info, longitude, latitude, location_outline_fk FROM locations"
    )
    locations = cursor.fetchall()

    result = []
    for loc in locations:
        outline = None
        if loc["location_outline_fk"]:
            cursor.execute(
                "SELECT id, points FROM location_outline WHERE id = ?",
                (loc["location_outline_fk"],),
            )
            outline_row = cursor.fetchone()
            if outline_row:
                outline = {
                    "id": outline_row["id"],
                    "points": outline_row["points"],
                }

        result.append(
            {
                "id": loc["id"],
                "info": loc["info"],
                "longitude": loc["longitude"],
                "latitude": loc["latitude"],
                "location_outline_fk": loc["location_outline_fk"],
                "location_outline": outline,
            }
        )

    conn.close()
    return result
