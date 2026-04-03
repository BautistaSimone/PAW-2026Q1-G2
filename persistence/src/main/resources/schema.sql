CREATE TABLE IF NOT EXISTS users (
	user_id SERIAL PRIMARY KEY,
	email VARCHAR(255) NOT NULL UNIQUE,
	password VARCHAR(255),
	username VARCHAR(255) NOT NULL,
	mod BOOLEAN NOT NULL
);

DROP TABLE IF EXISTS products CASCADE;
CREATE TABLE products (
	product_id SERIAL PRIMARY KEY,
	user_id INTEGER NOT NULL,
	title VARCHAR(255) NOT NULL,
	artist VARCHAR(255) NOT NULL,
	description TEXT NOT NULL,
	sleeve_condition NUMERIC NOT NULL,
	record_condition NUMERIC NOT NULL,
	neighborhood VARCHAR(255) NOT NULL,
	province VARCHAR(255) NOT NULL,
	published DATE NOT NULL,
	price NUMERIC NOT NULL,
	available BOOLEAN NOT NULL DEFAULT TRUE,
	FOREIGN KEY(user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS images (
	image_id SERIAL PRIMARY KEY,
	product_id INTEGER NOT NULL,
	data BYTEA NOT NULL,
	content_type VARCHAR(255) NOT NULL,
	FOREIGN KEY(product_id) REFERENCES products(product_id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS categories (
	category_id SERIAL PRIMARY KEY,
	name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS products_categories (
	product_id INTEGER NOT NULL,
	category_id INTEGER NOT NULL,
	PRIMARY KEY(product_id, category_id),
	FOREIGN KEY(category_id) REFERENCES categories(category_id) ON UPDATE NO ACTION ON DELETE NO ACTION,
	FOREIGN KEY(product_id) REFERENCES products(product_id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS purchases (
	purchase_id SERIAL PRIMARY KEY,
	product_id INTEGER NOT NULL,
	buyer_user_id INTEGER NOT NULL,
	seller_user_id INTEGER NOT NULL,
	date DATE NOT NULL,
	payment_method VARCHAR(255) NOT NULL,
	confirmed BOOLEAN NOT NULL,
	FOREIGN KEY(product_id) REFERENCES products(product_id) ON UPDATE NO ACTION ON DELETE NO ACTION,
	FOREIGN KEY(buyer_user_id) REFERENCES users(user_id) ON UPDATE NO ACTION ON DELETE NO ACTION,
	FOREIGN KEY(seller_user_id) REFERENCES users(user_id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- Seed default categories (genres) using more compatible EXISTS check instead of ON CONFLICT
INSERT INTO categories (name) SELECT 'Rock'        WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Rock');
INSERT INTO categories (name) SELECT 'Pop'         WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Pop');
INSERT INTO categories (name) SELECT 'Jazz'        WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Jazz');
INSERT INTO categories (name) SELECT 'Blues'       WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Blues');
INSERT INTO categories (name) SELECT 'Electrónica' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Electrónica');
INSERT INTO categories (name) SELECT 'Hip Hop'     WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Hip Hop');
INSERT INTO categories (name) SELECT 'Reggae'      WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Reggae');
INSERT INTO categories (name) SELECT 'Clásica'     WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Clásica');
INSERT INTO categories (name) SELECT 'Folk'        WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Folk');
INSERT INTO categories (name) SELECT 'Metal'       WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Metal');
INSERT INTO categories (name) SELECT 'Punk'        WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Punk');
INSERT INTO categories (name) SELECT 'Soul'        WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Soul');
INSERT INTO categories (name) SELECT 'Funk'        WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Funk');
INSERT INTO categories (name) SELECT 'Tango'       WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Tango');
INSERT INTO categories (name) SELECT 'Cumbia'      WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Cumbia');



