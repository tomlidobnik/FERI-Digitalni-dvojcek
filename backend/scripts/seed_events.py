#!/usr/bin/env python3
"""
Usage:
  python backend/scripts/seed_events.py --count 25 --public-ratio 0.6 --with-locations

"""
from __future__ import annotations

import argparse
import random
from datetime import datetime, timedelta
from pathlib import Path
import sqlite3

from backend.database import init_db, get_db_connection, DB_PATH


def ensure_user(conn: sqlite3.Connection, username: str | None = None) -> int:
    """Find any existing user; if none, create a fallback user and return id."""
    cursor = conn.cursor()
    cursor.execute("SELECT id, username FROM users LIMIT 1")
    row = cursor.fetchone()
    if row:
        return int(row[0])

    uname = username or "seed_user"
    email = f"{uname}@example.com"
    firstname = "Seed"
    lastname = "User"
    password = "password"

    cursor.execute(
        """
        INSERT INTO users (username, firstname, lastname, email, password)
        VALUES (?, ?, ?, ?, ?)
        """,
        (uname, firstname, lastname, email, password),
    )
    conn.commit()

    cursor.execute("SELECT id FROM users WHERE username = ?", (uname,))
    new_row = cursor.fetchone()
    if not new_row:
        raise RuntimeError("Failed to ensure fallback user")
    return int(new_row[0])


def ensure_locations(conn: sqlite3.Connection, count: int = 5) -> list[int]:
    """Create a few simple locations if none exist; return list of location ids."""
    cursor = conn.cursor()
    cursor.execute("SELECT id FROM locations")
    rows = cursor.fetchall()
    if rows:
        return [int(r[0]) for r in rows]

    ids: list[int] = []
    base_lat, base_lon = 46.5547, 15.6467
    for i in range(count):
        jitter_lat = base_lat + random.uniform(-0.05, 0.05)
        jitter_lon = base_lon + random.uniform(-0.05, 0.05)
        cursor.execute(
            """
            INSERT INTO locations (info, longitude, latitude, location_outline_fk)
            VALUES (?, ?, ?, NULL)
            """,
            (f"Seed Location {i+1}", float(jitter_lon), float(jitter_lat)),
        )
        ids.append(cursor.lastrowid)
    conn.commit()
    return ids


def generate_event(i: int, user_fk: int, location_ids: list[int] | None, public_ratio: float) -> dict:
    """Generate a single event payload as a dict matching DB columns (except id)."""
    titles = [
        "Community Meetup",
        "Tech Talk",
        "Hack Night",
        "Workshop",
        "Coffee & Code",
        "Board Games",
        "Outdoor Run",
        "Book Club",
        "Art Jam",
        "Movie Night",
    ]
    tags = [None, "social", "tech", "fitness", "education", "fun"]

    start_offset_days = random.randint(-10, 20)
    start_time = datetime.utcnow() + timedelta(days=start_offset_days, hours=random.randint(0, 23))
    duration_hours = random.choice([1, 2, 3, 4])
    end_time = start_time + timedelta(hours=duration_hours)

    public_flag = random.random() < max(0.0, min(1.0, public_ratio))

    location_fk = None
    if location_ids:
        if random.random() < 0.7:
            location_fk = random.choice(location_ids)

    return {
        "user_fk": user_fk,
        "title": f"{random.choice(titles)} #{i+1}",
        "description": "Auto-generated seed event.",
        "start_date": start_time.isoformat(timespec="seconds"),
        "end_date": end_time.isoformat(timespec="seconds"),
        "location_fk": location_fk,
        "public": 1 if public_flag else 0,
        "tag": random.choice(tags),
    }


def seed_events(count: int, public_ratio: float, with_locations: bool, username: str | None, seed: int | None) -> int:
    if seed is not None:
        random.seed(seed)

    init_db()

    conn = get_db_connection()
    try:
        user_fk = ensure_user(conn, username)
        location_ids = ensure_locations(conn, count=max(3, count // 5)) if with_locations else []

        cursor = conn.cursor()
        created = 0
        for i in range(count):
            ev = generate_event(i, user_fk, location_ids, public_ratio)
            cursor.execute(
                """
                INSERT INTO events (user_fk, title, description, start_date, end_date, location_fk, public, tag)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    ev["user_fk"],
                    ev["title"],
                    ev["description"],
                    ev["start_date"],
                    ev["end_date"],
                    ev["location_fk"],
                    ev["public"],
                    ev["tag"],
                ),
            )
            created += 1
        conn.commit()
        return created
    finally:
        conn.close()


def main() -> None:
    parser = argparse.ArgumentParser(description="Seed the database with sample events")
    parser.add_argument("--count", type=int, default=20, help="Number of events to create")
    parser.add_argument("--public-ratio", type=float, default=0.6, help="Fraction of public events (0-1)")
    parser.add_argument("--with-locations", action="store_true", help="Also seed simple locations and assign them")
    parser.add_argument("--username", type=str, default=None, help="Username to own the events (fallback if DB empty)")
    parser.add_argument("--seed", type=int, default=None, help="Random seed for reproducibility")

    args = parser.parse_args()

    created = seed_events(
        count=args.count,
        public_ratio=args.public_ratio,
        with_locations=args.with_locations,
        username=args.username,
        seed=args.seed,
    )

    print(f"Created {created} events in {DB_PATH}")


if __name__ == "__main__":
    main()
