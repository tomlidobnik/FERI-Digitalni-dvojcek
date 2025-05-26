CREATE TABLE "friends"(
    id SERIAL PRIMARY KEY,
    user1_fk INT REFERENCES users(id) ON DELETE CASCADE,
    user2_fk INT REFERENCES users(id) ON DELETE CASCADE,
    status INT NOT NULL
);
