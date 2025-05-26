CREATE TABLE "chat_messages" (
    id SERIAL PRIMARY KEY,
    user_fk INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    event_fk INT NOT NULL REFERENCES events(id) ON DELETE CASCADE
);
