from fastapi import APIRouter, HTTPException, status, Depends, Header
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
from database import (
    get_db_connection,
    User,
    Event,
)
from auth import hash_password, verify_password, create_jwt, decode_jwt

router = APIRouter(prefix="/api/user", tags=["user"])


# Pydantic models
class UserCreate(BaseModel):
    username: str
    firstname: str
    lastname: str
    email: str
    password: str


class UserUpdate(BaseModel):
    username: str
    firstname: str
    lastname: str
    email: str


class UserResponse(BaseModel):
    id: Optional[int]
    username: str
    first_name: str
    last_name: str
    email: str


class LoginRequest(BaseModel):
    username: str
    password: str


class TokenResponse(BaseModel):
    token: str


class StatsResponse(BaseModel):
    total_events: int
    active_events: int
    upcoming_events: int
    total_users: int


def get_authenticated_user(authorization: Optional[str] = Header(None)) -> str:
    """Extract and verify the user from JWT token."""
    if not authorization:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing authorization header"
        )

    try:
        scheme, token = authorization.split()
        if scheme.lower() != "bearer":
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid authorization scheme"
            )
        payload = decode_jwt(token)
        username = payload.get("sub")
        if not username:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token"
            )
        return username
    except ValueError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid authorization header"
        )


@router.post("/create", status_code=status.HTTP_201_CREATED)
def create_user(user_data: UserCreate):
    """Create a new user."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute(
        "SELECT id FROM users WHERE username = ? OR email = ?",
        (user_data.username, user_data.email),
    )
    if cursor.fetchone():
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Username or email already exists",
        )

    hashed_password = hash_password(user_data.password)

    try:
        cursor.execute(
            """
            INSERT INTO users (username, firstname, lastname, email, password)
            VALUES (?, ?, ?, ?, ?)
        """,
            (
                user_data.username,
                user_data.firstname,
                user_data.lastname,
                user_data.email,
                hashed_password,
            ),
        )
        conn.commit()
        conn.close()
        return {"status": "created"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to create user",
        )


@router.post("/validate")
def validate_user(login_data: LoginRequest):
    """Validate user credentials."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT password FROM users WHERE username = ?", (login_data.username,))
    row = cursor.fetchone()
    conn.close()

    if not row:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid username or password"
        )

    if not verify_password(login_data.password, row["password"]):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid username or password"
        )

    return {"status": "valid"}


@router.post("/token", response_model=TokenResponse)
def generate_token(login_data: LoginRequest):
    """Generate JWT token for user."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT password FROM users WHERE username = ?", (login_data.username,))
    row = cursor.fetchone()
    conn.close()

    if not row:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid username or password"
        )

    if not verify_password(login_data.password, row["password"]):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid username or password"
        )

    token = create_jwt(login_data.username)
    return {"token": token}


@router.get("/all", response_model=List[UserResponse])
def get_all_users():
    """Get all users."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT id, username, firstname, lastname, email FROM users")
    rows = cursor.fetchall()
    conn.close()

    users = [
        {
            "id": row["id"],
            "username": row["username"],
            "first_name": row["firstname"],
            "last_name": row["lastname"],
            "email": row["email"],
        }
        for row in rows
    ]

    return users


@router.get("/by_id/{user_id}", response_model=UserResponse)
def get_user_by_id(user_id: int):
    """Get user by ID."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute(
        "SELECT id, username, firstname, lastname, email FROM users WHERE id = ?",
        (user_id,),
    )
    row = cursor.fetchone()
    conn.close()

    if not row:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")

    return {
        "id": row["id"],
        "username": row["username"],
        "first_name": row["firstname"],
        "last_name": row["lastname"],
        "email": row["email"],
    }


@router.get("/by_token", response_model=UserResponse)
def get_user_by_token(username: str = Depends(get_authenticated_user)):
    """Get user by JWT token."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute(
        "SELECT id, username, firstname, lastname, email FROM users WHERE username = ?",
        (username,),
    )
    row = cursor.fetchone()
    conn.close()

    if not row:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")

    return {
        "id": row["id"],
        "username": row["username"],
        "first_name": row["firstname"],
        "last_name": row["lastname"],
        "email": row["email"],
    }


@router.put("/update")
def update_user(
    user_data: UserUpdate, username: str = Depends(get_authenticated_user)
):
    """Update user information."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT id FROM users WHERE username = ?", (username,))
    current_user = cursor.fetchone()

    if not current_user:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="User not found"
        )

    user_id = current_user["id"]

    if user_data.username != username:
        cursor.execute(
            "SELECT id FROM users WHERE username = ? AND id != ?",
            (user_data.username, user_id),
        )
        if cursor.fetchone():
            conn.close()
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST, detail="Username already taken"
            )

    cursor.execute(
        "SELECT id FROM users WHERE email = ? AND id != ?", (user_data.email, user_id)
    )
    if cursor.fetchone():
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST, detail="Email already taken"
        )

    try:
        cursor.execute(
            """
            UPDATE users
            SET username = ?, firstname = ?, lastname = ?, email = ?
            WHERE id = ?
        """,
            (user_data.username, user_data.firstname, user_data.lastname, user_data.email, user_id),
        )
        conn.commit()
        conn.close()
        return {"status": "updated"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to update user",
        )


@router.delete("/delete")
def delete_user(username: str = Depends(get_authenticated_user)):
    """Delete user account."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT id FROM users WHERE username = ?", (username,))
    user = cursor.fetchone()

    if not user:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="User not found"
        )

    try:
        cursor.execute("DELETE FROM users WHERE id = ?", (user["id"],))
        conn.commit()
        conn.close()
        return {"status": "deleted"}
    except Exception as e:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to delete user",
        )


@router.get("/stats", response_model=StatsResponse)
def get_stats():
    """Get statistics about users and events."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT COUNT(*) as count FROM users")
    total_users = cursor.fetchone()["count"]

    cursor.execute("SELECT COUNT(*) as count FROM events")
    total_events = cursor.fetchone()["count"]

    now = datetime.utcnow().isoformat()
    cursor.execute(
        "SELECT COUNT(*) as count FROM events WHERE start_date > ?", (now,)
    )
    upcoming_events = cursor.fetchone()["count"]

    cursor.execute(
        "SELECT COUNT(*) as count FROM events WHERE start_date <= ? AND end_date >= ?",
        (now, now),
    )
    active_events = cursor.fetchone()["count"]

    conn.close()

    return {
        "total_events": total_events,
        "active_events": active_events,
        "upcoming_events": upcoming_events,
        "total_users": total_users,
    }
