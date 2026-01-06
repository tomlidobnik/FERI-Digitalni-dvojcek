CREATE TABLE "friend_chat_messages" (
    id SERIAL PRIMARY KEY,
    user_fk INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    friend_fk INT NOT NULL REFERENCES friends(id) ON DELETE CASCADE
);
