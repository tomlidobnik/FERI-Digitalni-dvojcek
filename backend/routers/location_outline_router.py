from fastapi import APIRouter, HTTPException, status, Depends
from pydantic import BaseModel
from typing import Optional, List
from database import get_db_connection
from routers.user_router import get_authenticated_user

router = APIRouter(prefix="/api/location_outline", tags=["location_outline"])


# Pydantic models
class LocationOutlineCreate(BaseModel):
    points: str


class LocationOutlineResponse(BaseModel):
    id: Optional[int]
    points: str


@router.post("/create", status_code=status.HTTP_201_CREATED)
def create_location_outline(outline_data: LocationOutlineCreate):
    """Create a new location outline."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        cursor.execute(
            """
            INSERT INTO location_outline (points)
            VALUES (?)
        """,
            (outline_data.points,),
        )
        conn.commit()
        conn.close()
        return {"status": "created"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to create location outline",
        )


@router.put("/update")
def update_location_outline(outline_id: int, outline_data: LocationOutlineCreate):
    """Update a location outline."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT id FROM location_outline WHERE id = ?", (outline_id,))
    if not cursor.fetchone():
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Location outline not found"
        )

    try:
        cursor.execute(
            """
            UPDATE location_outline
            SET points = ?
            WHERE id = ?
        """,
            (outline_data.points, outline_id),
        )
        conn.commit()
        conn.close()
        return {"status": "updated"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to update location outline",
        )


@router.get("/all", response_model=List[LocationOutlineResponse])
def get_all_location_outlines():
    """Get all location outlines."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT id, points FROM location_outline")
    rows = cursor.fetchall()
    conn.close()

    return [
        {
            "id": row["id"],
            "points": row["points"],
        }
        for row in rows
    ]


@router.get("/by_id/{outline_id}", response_model=LocationOutlineResponse)
def get_location_outline_by_id(outline_id: int):
    """Get location outline by ID."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT id, points FROM location_outline WHERE id = ?", (outline_id,))
    row = cursor.fetchone()
    conn.close()

    if not row:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Location outline not found"
        )

    return {
        "id": row["id"],
        "points": row["points"],
    }


@router.delete("/delete/{outline_id}")
def delete_location_outline(outline_id: int):
    """Delete a location outline."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT id FROM location_outline WHERE id = ?", (outline_id,))
    if not cursor.fetchone():
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Location outline not found"
        )

    try:
        cursor.execute("DELETE FROM location_outline WHERE id = ?", (outline_id,))
        conn.commit()
        conn.close()
        return {"status": "deleted"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to delete location outline",
        )
