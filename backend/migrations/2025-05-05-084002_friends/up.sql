CREATE TABLE "friends"(
    id SERIAL PRIMARY KEY,
    user1_fk INT REFERENCES users(id) ON DELETE SET NULL,
    user2_fk INT REFERENCES users(id) ON DELETE SET NULL,
    status INT NOT NULL
);
