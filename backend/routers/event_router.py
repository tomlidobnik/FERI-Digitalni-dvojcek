from fastapi import APIRouter, HTTPException, status, Depends
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
from database import get_db_connection
from routers.user_router import get_authenticated_user

router = APIRouter(prefix="/api/event", tags=["event"])


# Pydantic models
class EventCreate(BaseModel):
    title: str
    description: str
    start_date: str
    end_date: str
    location_fk: Optional[int] = None
    public: bool = False
    tag: Optional[str] = None


class EventUpdate(BaseModel):
    id: int
    title: str
    description: str
    start_date: str
    end_date: str
    location_fk: Optional[int] = None
    public: bool = False
    tag: Optional[str] = None


class EventResponse(BaseModel):
    id: Optional[int]
    user_fk: Optional[int]
    title: str
    description: str
    start_date: str
    end_date: str
    location_fk: Optional[int]
    public: bool
    tag: Optional[str]


class AddEventAllowedUserRequest(BaseModel):
    user_id: int


async def get_user_id(username: str) -> int:
    """Get user ID from username."""
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT id FROM users WHERE username = ?", (username,))
    row = cursor.fetchone()
    conn.close()

    if not row:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="User not found"
        )
    return row["id"]


@router.post("/create", status_code=status.HTTP_201_CREATED)
async def create_event(
    event_data: EventCreate, username: str = Depends(get_authenticated_user)
):
    """Create a new event."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        user_id = await get_user_id(username)
    except HTTPException:
        conn.close()
        raise

    try:
        cursor.execute(
            """
            INSERT INTO events (user_fk, title, description, start_date, end_date, location_fk, public, tag)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """,
            (
                user_id,
                event_data.title,
                event_data.description,
                event_data.start_date,
                event_data.end_date,
                event_data.location_fk,
                event_data.public,
                event_data.tag,
            ),
        )
        conn.commit()
        conn.close()
        return {"status": "created"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to create event",
        )


@router.put("/update")
async def update_event(
    event_data: EventUpdate, username: str = Depends(get_authenticated_user)
):
    """Update an event."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        user_id = await get_user_id(username)
    except HTTPException:
        conn.close()
        raise

    cursor.execute(
        "SELECT user_fk FROM events WHERE id = ?", (event_data.id,)
    )
    event = cursor.fetchone()

    if not event:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Event not found"
        )

    if event["user_fk"] != user_id:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You don't have permission to update this event",
        )

    try:
        cursor.execute(
            """
            UPDATE events
            SET title = ?, description = ?, start_date = ?, end_date = ?, location_fk = ?, public = ?, tag = ?
            WHERE id = ?
        """,
            (
                event_data.title,
                event_data.description,
                event_data.start_date,
                event_data.end_date,
                event_data.location_fk,
                event_data.public,
                event_data.tag,
                event_data.id,
            ),
        )
        conn.commit()
        conn.close()
        return {"status": "updated"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to update event",
        )


@router.get("/all", response_model=List[EventResponse])
def get_all_events():
    """Get all events."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute(
        "SELECT id, user_fk, title, description, start_date, end_date, location_fk, public, tag FROM events"
    )
    rows = cursor.fetchall()
    conn.close()

    return [
        {
            "id": row["id"],
            "user_fk": row["user_fk"],
            "title": row["title"],
            "description": row["description"],
            "start_date": row["start_date"],
            "end_date": row["end_date"],
            "location_fk": row["location_fk"],
            "public": bool(row["public"]),
            "tag": row["tag"],
        }
        for row in rows
    ]


@router.get("/by_id/{event_id}", response_model=EventResponse)
async def get_event_by_id(
    event_id: int, username: str = Depends(get_authenticated_user)
):
    """Get event by ID."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute(
        "SELECT id, user_fk, title, description, start_date, end_date, location_fk, public, tag FROM events WHERE id = ?",
        (event_id,),
    )
    row = cursor.fetchone()

    if not row:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Event not found"
        )

    if not row["public"]:
        try:
            user_id = await get_user_id(username)
        except HTTPException:
            conn.close()
            raise

        cursor.execute(
            "SELECT event_id FROM event_allowed_users WHERE event_id = ? AND user_id = ?",
            (event_id, user_id),
        )
        if not cursor.fetchone():
            conn.close()
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="You don't have access to this event",
            )

    conn.close()

    return {
        "id": row["id"],
        "user_fk": row["user_fk"],
        "title": row["title"],
        "description": row["description"],
        "start_date": row["start_date"],
        "end_date": row["end_date"],
        "location_fk": row["location_fk"],
        "public": bool(row["public"]),
        "tag": row["tag"],
    }


@router.get("/my", response_model=List[EventResponse])
async def get_user_events(username: str = Depends(get_authenticated_user)):
    """Get events created by the authenticated user."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        user_id = await get_user_id(username)
    except HTTPException:
        conn.close()
        raise

    cursor.execute(
        "SELECT id, user_fk, title, description, start_date, end_date, location_fk, public, tag FROM events WHERE user_fk = ?",
        (user_id,),
    )
    rows = cursor.fetchall()
    conn.close()

    return [
        {
            "id": row["id"],
            "user_fk": row["user_fk"],
            "title": row["title"],
            "description": row["description"],
            "start_date": row["start_date"],
            "end_date": row["end_date"],
            "location_fk": row["location_fk"],
            "public": bool(row["public"]),
            "tag": row["tag"],
        }
        for row in rows
    ]


@router.delete("/delete/{event_id}")
async def delete_event(
    event_id: int, username: str = Depends(get_authenticated_user)
):
    """Delete an event."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        user_id = await get_user_id(username)
    except HTTPException:
        conn.close()
        raise

    cursor.execute("SELECT user_fk FROM events WHERE id = ?", (event_id,))
    event = cursor.fetchone()

    if not event:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Event not found"
        )

    if event["user_fk"] != user_id:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You don't have permission to delete this event",
        )

    try:
        cursor.execute("DELETE FROM events WHERE id = ?", (event_id,))
        conn.commit()
        conn.close()
        return {"status": "deleted"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to delete event",
        )


@router.get("/available", response_model=List[EventResponse])
def get_available_events():
    """Get all public events."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute(
        "SELECT id, user_fk, title, description, start_date, end_date, location_fk, public, tag FROM events WHERE public = 1"
    )
    rows = cursor.fetchall()
    conn.close()

    return [
        {
            "id": row["id"],
            "user_fk": row["user_fk"],
            "title": row["title"],
            "description": row["description"],
            "start_date": row["start_date"],
            "end_date": row["end_date"],
            "location_fk": row["location_fk"],
            "public": bool(row["public"]),
            "tag": row["tag"],
        }
        for row in rows
    ]


@router.put("/make_public/{event_id}")
async def make_event_public(
    event_id: int, username: str = Depends(get_authenticated_user)
):
    """Make an event public."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        user_id = await get_user_id(username)
    except HTTPException:
        conn.close()
        raise

    cursor.execute("SELECT user_fk FROM events WHERE id = ?", (event_id,))
    event = cursor.fetchone()

    if not event:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Event not found"
        )

    if event["user_fk"] != user_id:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You don't have permission to modify this event",
        )

    try:
        cursor.execute("UPDATE events SET public = 1 WHERE id = ?", (event_id,))
        conn.commit()
        conn.close()
        return {"status": "event made public"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to make event public",
        )


@router.put("/make_private/{event_id}")
async def make_event_private(
    event_id: int, username: str = Depends(get_authenticated_user)
):
    """Make an event private."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        user_id = await get_user_id(username)
    except HTTPException:
        conn.close()
        raise

    cursor.execute("SELECT user_fk FROM events WHERE id = ?", (event_id,))
    event = cursor.fetchone()

    if not event:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Event not found"
        )

    if event["user_fk"] != user_id:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You don't have permission to modify this event",
        )

    try:
        cursor.execute("UPDATE events SET public = 0 WHERE id = ?", (event_id,))
        conn.commit()
        conn.close()
        return {"status": "event made private"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to make event private",
        )


@router.post("/add_user_to_event/{event_id}")
async def add_user_to_private_event(
    event_id: int,
    request: AddEventAllowedUserRequest,
    username: str = Depends(get_authenticated_user),
):
    """Add a user to a private event."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        user_id = await get_user_id(username)
    except HTTPException:
        conn.close()
        raise

    cursor.execute("SELECT user_fk FROM events WHERE id = ?", (event_id,))
    event = cursor.fetchone()

    if not event:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Event not found"
        )

    if event["user_fk"] != user_id:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You don't have permission to modify this event",
        )

    try:
        cursor.execute(
            "INSERT INTO event_allowed_users (event_id, user_id) VALUES (?, ?)",
            (event_id, request.user_id),
        )
        conn.commit()
        conn.close()
        return {"status": "user added to event"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to add user to event",
        )


@router.post("/join_public_event/{event_id}")
async def join_public_event(
    event_id: int, username: str = Depends(get_authenticated_user)
):
    """Join a public event."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        user_id = await get_user_id(username)
    except HTTPException:
        conn.close()
        raise

    cursor.execute("SELECT public FROM events WHERE id = ?", (event_id,))
    event = cursor.fetchone()

    if not event:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Event not found"
        )

    if not event["public"]:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN, detail="Event is not public"
        )

    try:
        cursor.execute(
            "INSERT INTO event_users (event_id, user_id) VALUES (?, ?)",
            (event_id, user_id),
        )
        conn.commit()
        conn.close()
        return {"status": "joined event"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to join event",
        )


@router.post("/leave_event/{event_id}")
async def leave_event(event_id: int, username: str = Depends(get_authenticated_user)):
    """Leave an event."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        user_id = await get_user_id(username)
    except HTTPException:
        conn.close()
        raise

    try:
        cursor.execute(
            "DELETE FROM event_users WHERE event_id = ? AND user_id = ?",
            (event_id, user_id),
        )
        conn.commit()
        conn.close()
        return {"status": "left event"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to leave event",
        )


@router.delete("/remove_user_from_event/{event_id}/{user_id}")
async def remove_user_from_private_event(
    event_id: int,
    user_id: int,
    username: str = Depends(get_authenticated_user),
):
    """Remove a user from a private event."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        auth_user_id = await get_user_id(username)
    except HTTPException:
        conn.close()
        raise

    cursor.execute("SELECT user_fk FROM events WHERE id = ?", (event_id,))
    event = cursor.fetchone()

    if not event:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Event not found"
        )

    if event["user_fk"] != auth_user_id:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You don't have permission to modify this event",
        )

    try:
        cursor.execute(
            "DELETE FROM event_allowed_users WHERE event_id = ? AND user_id = ?",
            (event_id, user_id),
        )
        conn.commit()
        conn.close()
        return {"status": "user removed from event"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to remove user from event",
        )


@router.get("/get_users/{event_id}/", response_model=List[dict])
def get_user_at_events(event_id: int):
    """Get all users in an event."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute(
        """
        SELECT u.id, u.username, u.firstname, u.lastname, u.email
        FROM users u
        JOIN event_users eu ON u.id = eu.user_id
        WHERE eu.event_id = ?
    """,
        (event_id,),
    )
    rows = cursor.fetchall()
    conn.close()

    return [
        {
            "id": row["id"],
            "username": row["username"],
            "first_name": row["firstname"],
            "last_name": row["lastname"],
            "email": row["email"],
        }
        for row in rows
    ]
