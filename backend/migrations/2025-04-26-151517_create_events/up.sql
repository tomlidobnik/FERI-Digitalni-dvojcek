CREATE TABLE "events" (
    id SERIAL PRIMARY KEY,
    user_fk INT REFERENCES users(id) ON DELETE SET NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL
);
