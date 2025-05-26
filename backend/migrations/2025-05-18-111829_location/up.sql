CREATE TABLE locations (
    id SERIAL PRIMARY KEY,
    info TEXT,
    longitude REAL,
    latitude REAL,
    location_outline_fk INT REFERENCES location_outline(id),
    CHECK (
        (
            longitude IS NOT NULL AND
            latitude IS NOT NULL AND
            location_outline_fk IS NULL
        ) OR (
            longitude IS NULL AND
            latitude IS NULL AND
            location_outline_fk IS NOT NULL
        )
    )
);