from fastapi import APIRouter, HTTPException, status, Depends
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
from database import get_db_connection
from routers.user_router import get_authenticated_user

router = APIRouter(prefix="/api/chat", tags=["chat"])


# Pydantic models
class ChatMessageResponse(BaseModel):
    id: Optional[int]
    user_fk: int
    message: str
    created_at: str
    event_fk: int


class FriendChatMessageResponse(BaseModel):
    id: Optional[int]
    user_fk: int
    message: str
    created_at: str
    friend_fk: int


@router.get("/history/{event_id}", response_model=List[ChatMessageResponse])
def get_chat_history(event_id: int):
    """Get chat history for an event."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute(
        """
        SELECT id, user_fk, message, created_at, event_fk
        FROM chat_messages
        WHERE event_fk = ?
        ORDER BY created_at ASC
    """,
        (event_id,),
    )
    rows = cursor.fetchall()
    conn.close()

    return [
        {
            "id": row["id"],
            "user_fk": row["user_fk"],
            "message": row["message"],
            "created_at": row["created_at"],
            "event_fk": row["event_fk"],
        }
        for row in rows
    ]


@router.get("/friend_history/{friend_id}", response_model=List[FriendChatMessageResponse])
def get_friend_chat_history(friend_id: int):
    """Get chat history for a friend connection."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute(
        """
        SELECT id, user_fk, message, created_at, friend_fk
        FROM friend_chat_messages
        WHERE friend_fk = ?
        ORDER BY created_at ASC
    """,
        (friend_id,),
    )
    rows = cursor.fetchall()
    conn.close()

    return [
        {
            "id": row["id"],
            "user_fk": row["user_fk"],
            "message": row["message"],
            "created_at": row["created_at"],
            "friend_fk": row["friend_fk"],
        }
        for row in rows
    ]
