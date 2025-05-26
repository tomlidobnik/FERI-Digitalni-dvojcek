CREATE TABLE events (
    id SERIAL PRIMARY KEY,
    user_fk INT REFERENCES users(id) ON DELETE SET NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    location_fk INT REFERENCES locations(id) ON DELETE SET NULL,
    public BOOLEAN NOT NULL DEFAULT TRUE
);