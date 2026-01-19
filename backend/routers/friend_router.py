from fastapi import APIRouter, HTTPException, status, Depends, Header
from pydantic import BaseModel
from typing import Optional, List
from database import get_db_connection, Friend
from routers.user_router import get_authenticated_user

router = APIRouter(prefix="/api/user/friends", tags=["friends"])


# Pydantic models
class FriendRequest(BaseModel):
    username: str


class FriendStatusResponse(BaseModel):
    status: str
    friendship_id: Optional[int] = None


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


@router.post("/request")
async def friend_request(
    request_data: FriendRequest, username: str = Depends(get_authenticated_user)
):
    """Send a friend request."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        this_user_id = await get_user_id(username)
        friend_user_id = await get_user_id(request_data.username)
    except HTTPException:
        conn.close()
        raise

    cursor.execute(
        """
        SELECT id, status FROM friends
        WHERE (user1_fk = ? AND user2_fk = ?) OR (user1_fk = ? AND user2_fk = ?)
    """,
        (this_user_id, friend_user_id, friend_user_id, this_user_id),
    )
    existing = cursor.fetchone()

    if existing:
        if existing["status"] == 0:
            conn.close()
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Already friends with this user",
            )
        elif existing["status"] == this_user_id:
            conn.close()
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Friend request already sent",
            )
        elif existing["status"] == friend_user_id:
            cursor.execute(
                "UPDATE friends SET status = 0 WHERE id = ?", (existing["id"],)
            )
            conn.commit()
            conn.close()
            return {"status": "friend request accepted"}

    try:
        cursor.execute(
            """
            INSERT INTO friends (user1_fk, user2_fk, status)
            VALUES (?, ?, ?)
        """,
            (this_user_id, friend_user_id, this_user_id),
        )
        conn.commit()
        conn.close()
        return {"status": "friend request sent"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to send friend request",
        )


@router.get("/status/{friend_username}", response_model=FriendStatusResponse)
async def friend_status(
    friend_username: str, username: str = Depends(get_authenticated_user)
):
    """Get friend status with another user."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        this_user_id = await get_user_id(username)
        friend_user_id = await get_user_id(friend_username)
    except HTTPException:
        conn.close()
        raise

    cursor.execute(
        """
        SELECT id, status FROM friends
        WHERE (user1_fk = ? AND user2_fk = ?) OR (user1_fk = ? AND user2_fk = ?)
    """,
        (this_user_id, friend_user_id, friend_user_id, this_user_id),
    )
    friendship = cursor.fetchone()
    conn.close()

    if not friendship:
        return {"status": "Not Friends", "friendship_id": None}

    status_code = friendship["status"]
    friendship_id = friendship["id"]

    if status_code == 0:
        return {"status": "Friends", "friendship_id": friendship_id}
    elif status_code == this_user_id:
        return {"status": "Request Sent", "friendship_id": friendship_id}
    elif status_code == friend_user_id:
        return {"status": "Accept Friend Request", "friendship_id": friendship_id}
    else:
        return {"status": "Request Pending", "friendship_id": friendship_id}


@router.get("/list", response_model=List[str])
async def list_friends(username: str = Depends(get_authenticated_user)):
    """Get list of friend usernames."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        this_user_id = await get_user_id(username)
    except HTTPException:
        conn.close()
        raise

    cursor.execute(
        """
        SELECT user1_fk, user2_fk FROM friends
        WHERE status = 0 AND (user1_fk = ? OR user2_fk = ?)
    """,
        (this_user_id, this_user_id),
    )
    friendships = cursor.fetchall()

    friend_ids = []
    for friendship in friendships:
        if friendship["user1_fk"] == this_user_id:
            friend_ids.append(friendship["user2_fk"])
        else:
            friend_ids.append(friendship["user1_fk"])

    if not friend_ids:
        conn.close()
        return []

    placeholders = ",".join("?" * len(friend_ids))
    cursor.execute(
        f"SELECT username FROM users WHERE id IN ({placeholders})", friend_ids
    )
    friends = [row["username"] for row in cursor.fetchall()]
    conn.close()

    return friends


@router.get("/list_ids", response_model=List[int])
async def list_friends_ids(username: str = Depends(get_authenticated_user)):
    """Get list of friendship IDs."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        this_user_id = await get_user_id(username)
    except HTTPException:
        conn.close()
        raise

    cursor.execute(
        """
        SELECT id FROM friends
        WHERE status = 0 AND (user1_fk = ? OR user2_fk = ?)
    """,
        (this_user_id, this_user_id),
    )
    friendships = cursor.fetchall()
    conn.close()

    return [f["id"] for f in friendships]


@router.delete("/remove/{friend_username}")
async def remove_friend(
    friend_username: str, username: str = Depends(get_authenticated_user)
):
    """Remove a friend."""
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        this_user_id = await get_user_id(username)
        friend_user_id = await get_user_id(friend_username)
    except HTTPException:
        conn.close()
        raise

    try:
        cursor.execute(
            """
            DELETE FROM friends
            WHERE (user1_fk = ? AND user2_fk = ?) OR (user1_fk = ? AND user2_fk = ?)
        """,
            (this_user_id, friend_user_id, friend_user_id, this_user_id),
        )
        conn.commit()

        if cursor.rowcount == 0:
            conn.close()
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Friendship not found"
            )

        conn.close()
        return {"status": "friend removed"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to remove friend",
        )
