import sqlite3
from datetime import datetime
from typing import Optional
from pathlib import Path

# Database path
DB_PATH = Path(__file__).parent / "app.db"


def get_db_connection():
    """Get a database connection with row factory."""
    conn = sqlite3.connect(str(DB_PATH))
    conn.row_factory = sqlite3.Row
    return conn


def init_db():
    """Initialize the database with all required tables."""
    conn = get_db_connection()
    cursor = conn.cursor()

    # Users table
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            firstname TEXT NOT NULL,
            lastname TEXT NOT NULL,
            email TEXT UNIQUE NOT NULL,
            password TEXT NOT NULL
        )
    """
    )

    # Friends table
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS friends (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user1_fk INTEGER,
            user2_fk INTEGER,
            status INTEGER NOT NULL,
            FOREIGN KEY (user1_fk) REFERENCES users(id),
            FOREIGN KEY (user2_fk) REFERENCES users(id)
        )
    """
    )

    # Location outline table
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS location_outline (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            points TEXT NOT NULL
        )
    """
    )

    # Locations table
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS locations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            info TEXT,
            longitude REAL,
            latitude REAL,
            location_outline_fk INTEGER,
            FOREIGN KEY (location_outline_fk) REFERENCES location_outline(id)
        )
    """
    )

    # Events table
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS events (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_fk INTEGER,
            title TEXT NOT NULL,
            description TEXT NOT NULL,
            start_date TEXT NOT NULL,
            end_date TEXT NOT NULL,
            location_fk INTEGER,
            public BOOLEAN NOT NULL DEFAULT 0,
            tag TEXT,
            FOREIGN KEY (user_fk) REFERENCES users(id),
            FOREIGN KEY (location_fk) REFERENCES locations(id)
        )
    """
    )

    # Event allowed users table (for private events)
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS event_allowed_users (
            event_id INTEGER NOT NULL,
            user_id INTEGER NOT NULL,
            PRIMARY KEY (event_id, user_id),
            FOREIGN KEY (event_id) REFERENCES events(id),
            FOREIGN KEY (user_id) REFERENCES users(id)
        )
    """
    )

    # Event users table (for event participation)
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS event_users (
            event_id INTEGER NOT NULL,
            user_id INTEGER NOT NULL,
            PRIMARY KEY (event_id, user_id),
            FOREIGN KEY (event_id) REFERENCES events(id),
            FOREIGN KEY (user_id) REFERENCES users(id)
        )
    """
    )

    # Chat messages table
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS chat_messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_fk INTEGER NOT NULL,
            message TEXT NOT NULL,
            created_at TEXT NOT NULL,
            event_fk INTEGER NOT NULL,
            FOREIGN KEY (user_fk) REFERENCES users(id),
            FOREIGN KEY (event_fk) REFERENCES events(id)
        )
    """
    )

    # Friend chat messages table
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS friend_chat_messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_fk INTEGER NOT NULL,
            message TEXT NOT NULL,
            created_at TEXT NOT NULL,
            friend_fk INTEGER NOT NULL,
            FOREIGN KEY (user_fk) REFERENCES users(id),
            FOREIGN KEY (friend_fk) REFERENCES friends(id)
        )
    """
    )

    conn.commit()
    conn.close()


# Models
class User:
    def __init__(
        self,
        username: str,
        firstname: str,
        lastname: str,
        email: str,
        password: str,
        id: Optional[int] = None,
    ):
        self.id = id
        self.username = username
        self.firstname = firstname
        self.lastname = lastname
        self.email = email
        self.password = password

    def to_dict(self):
        return {
            "id": self.id,
            "username": self.username,
            "first_name": self.firstname,
            "last_name": self.lastname,
            "email": self.email,
        }

    @staticmethod
    def from_row(row):
        return User(
            id=row["id"],
            username=row["username"],
            firstname=row["firstname"],
            lastname=row["lastname"],
            email=row["email"],
            password=row["password"],
        )


class Friend:
    def __init__(self, user1_fk: int, user2_fk: int, status: int, id: Optional[int] = None):
        self.id = id
        self.user1_fk = user1_fk
        self.user2_fk = user2_fk
        self.status = status

    def to_dict(self):
        return {
            "id": self.id,
            "user1_fk": self.user1_fk,
            "user2_fk": self.user2_fk,
            "status": self.status,
        }

    @staticmethod
    def from_row(row):
        return Friend(
            id=row["id"],
            user1_fk=row["user1_fk"],
            user2_fk=row["user2_fk"],
            status=row["status"],
        )


class LocationOutline:
    def __init__(self, points: str, id: Optional[int] = None):
        self.id = id
        self.points = points

    def to_dict(self):
        return {"id": self.id, "points": self.points}

    @staticmethod
    def from_row(row):
        return LocationOutline(id=row["id"], points=row["points"])


class Location:
    def __init__(
        self,
        info: Optional[str] = None,
        longitude: Optional[float] = None,
        latitude: Optional[float] = None,
        location_outline_fk: Optional[int] = None,
        id: Optional[int] = None,
    ):
        self.id = id
        self.info = info
        self.longitude = longitude
        self.latitude = latitude
        self.location_outline_fk = location_outline_fk

    def to_dict(self):
        return {
            "id": self.id,
            "info": self.info,
            "longitude": self.longitude,
            "latitude": self.latitude,
            "location_outline_fk": self.location_outline_fk,
        }

    @staticmethod
    def from_row(row):
        return Location(
            id=row["id"],
            info=row["info"],
            longitude=row["longitude"],
            latitude=row["latitude"],
            location_outline_fk=row["location_outline_fk"],
        )


class Event:
    def __init__(
        self,
        title: str,
        description: str,
        start_date: str,
        end_date: str,
        public: bool = False,
        user_fk: Optional[int] = None,
        location_fk: Optional[int] = None,
        tag: Optional[str] = None,
        id: Optional[int] = None,
    ):
        self.id = id
        self.user_fk = user_fk
        self.title = title
        self.description = description
        self.start_date = start_date
        self.end_date = end_date
        self.location_fk = location_fk
        self.public = public
        self.tag = tag

    def to_dict(self):
        return {
            "id": self.id,
            "user_fk": self.user_fk,
            "title": self.title,
            "description": self.description,
            "start_date": self.start_date,
            "end_date": self.end_date,
            "location_fk": self.location_fk,
            "public": self.public,
            "tag": self.tag,
        }

    @staticmethod
    def from_row(row):
        return Event(
            id=row["id"],
            user_fk=row["user_fk"],
            title=row["title"],
            description=row["description"],
            start_date=row["start_date"],
            end_date=row["end_date"],
            location_fk=row["location_fk"],
            public=bool(row["public"]),
            tag=row["tag"],
        )


class ChatMessage:
    def __init__(
        self,
        user_fk: int,
        message: str,
        created_at: str,
        event_fk: int,
        id: Optional[int] = None,
    ):
        self.id = id
        self.user_fk = user_fk
        self.message = message
        self.created_at = created_at
        self.event_fk = event_fk

    def to_dict(self):
        return {
            "id": self.id,
            "user_fk": self.user_fk,
            "message": self.message,
            "created_at": self.created_at,
            "event_fk": self.event_fk,
        }

    @staticmethod
    def from_row(row):
        return ChatMessage(
            id=row["id"],
            user_fk=row["user_fk"],
            message=row["message"],
            created_at=row["created_at"],
            event_fk=row["event_fk"],
        )


class FriendChatMessage:
    def __init__(
        self,
        user_fk: int,
        message: str,
        created_at: str,
        friend_fk: int,
        id: Optional[int] = None,
    ):
        self.id = id
        self.user_fk = user_fk
        self.message = message
        self.created_at = created_at
        self.friend_fk = friend_fk

    def to_dict(self):
        return {
            "id": self.id,
            "user_fk": self.user_fk,
            "message": self.message,
            "created_at": self.created_at,
            "friend_fk": self.friend_fk,
        }

    @staticmethod
    def from_row(row):
        return FriendChatMessage(
            id=row["id"],
            user_fk=row["user_fk"],
            message=row["message"],
            created_at=row["created_at"],
            friend_fk=row["friend_fk"],
        )
