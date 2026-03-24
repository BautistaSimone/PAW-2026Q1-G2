CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    username TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    product_id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    artist TEXT NOT NULL,
    genre TEXT NOT NULL,
    vinyl_condition TEXT NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    image_url TEXT,
    description TEXT,
    published DATE NOT NULL DEFAULT CURRENT_DATE
);
