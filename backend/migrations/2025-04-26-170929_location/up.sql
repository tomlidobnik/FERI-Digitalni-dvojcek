CREATE TABLE "locations" (
    id SERIAL PRIMARY KEY,
    info TEXT,
    longitude DECIMAL(9,6) NOT NULL,
    latitude DECIMAL(9,6) NOT NULL
);
