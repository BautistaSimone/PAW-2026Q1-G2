CREATE TABLE IF NOT EXISTS users (
	user_id SERIAL PRIMARY KEY,
	email VARCHAR(255) NOT NULL UNIQUE,
	password VARCHAR(255) NOT NULL,
	username VARCHAR(255) NOT NULL,
	enabled BOOLEAN NOT NULL DEFAULT false, 
	mod BOOLEAN NOT NULL,
	first_name VARCHAR(100),
	last_name VARCHAR(100),
	street_name VARCHAR(255),
	street_number VARCHAR(20),
	neighborhood VARCHAR(100),
	province VARCHAR(100),
	extra_address_info VARCHAR(500),
	cbu_cvu VARCHAR(22),
	banned BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS password_tokens (
	token_id SERIAL PRIMARY KEY,
	user_id INTEGER NOT NULL,
	token VARCHAR(255) NOT NULL UNIQUE,
	expiration_date TIMESTAMP NOT NULL,
	FOREIGN KEY(user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS verification_tokens (
	token_id SERIAL PRIMARY KEY,
	user_id INTEGER NOT NULL,
	token VARCHAR(255) NOT NULL UNIQUE,
	expiration_date TIMESTAMP NOT NULL,
	FOREIGN KEY(user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS products (
	product_id SERIAL PRIMARY KEY,
	user_id INTEGER NOT NULL,
	title VARCHAR(255) NOT NULL,
	artist VARCHAR(255) NOT NULL,
	record_label VARCHAR(255) NOT NULL DEFAULT '',
	catalog_number VARCHAR(255) NOT NULL DEFAULT '',
	edition_country VARCHAR(255) NOT NULL DEFAULT '',
	description TEXT NOT NULL,
	sleeve_condition NUMERIC NOT NULL,
	record_condition NUMERIC NOT NULL,
	published DATE NOT NULL,
	price NUMERIC NOT NULL,
	state VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
	FOREIGN KEY(user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_wishlist_products (
	product_id INTEGER NOT NULL,
	user_id INTEGER NOT NULL,
	PRIMARY KEY(product_id, user_id),
	FOREIGN KEY(user_id) REFERENCES users(user_id) ON UPDATE NO ACTION ON DELETE NO ACTION,
	FOREIGN KEY(product_id) REFERENCES products(product_id) ON UPDATE NO ACTION ON DELETE NO ACTION
);


CREATE TABLE IF NOT EXISTS images (
	image_id SERIAL PRIMARY KEY,
	product_id INTEGER NOT NULL,
	data BLOB NOT NULL,
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

CREATE TABLE IF NOT EXISTS reviews (
	review_id SERIAL PRIMARY KEY,
	purchase_id INTEGER NOT NULL UNIQUE,
	seller_id INTEGER NOT NULL,
	buyer_id INTEGER NOT NULL,
	score INTEGER NOT NULL CHECK (score >= 0 AND score <= 5),
	review TEXT,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY(purchase_id) REFERENCES purchases(purchase_id) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY(seller_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY(buyer_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS wishlist_products (
	user_id INTEGER NOT NULL,
	product_id INTEGER NOT NULL,
	PRIMARY KEY(user_id, product_id),
	FOREIGN KEY(product_id) REFERENCES products(product_id) ON UPDATE NO ACTION ON DELETE NO ACTION,
	FOREIGN KEY(user_id) REFERENCES users(user_id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS reports (
	report_id SERIAL PRIMARY KEY,
	product_id INTEGER NOT NULL,
	owner_user_id INTEGER NOT NULL,
	reporter_user_id INTEGER NOT NULL,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	UNIQUE(product_id, reporter_user_id),
	FOREIGN KEY(product_id) REFERENCES products(product_id) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY(owner_user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY(reporter_user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_follows (
	follower_id INTEGER NOT NULL,
	followed_id INTEGER NOT NULL,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (follower_id, followed_id),
	FOREIGN KEY (follower_id) REFERENCES users(user_id) ON DELETE CASCADE,
	FOREIGN KEY (followed_id) REFERENCES users(user_id) ON DELETE CASCADE,
	CHECK (follower_id <> followed_id)
);

CREATE TABLE IF NOT EXISTS user_favorite_categories (
	user_id INTEGER NOT NULL,
	category_id INTEGER NOT NULL,
	PRIMARY KEY (user_id, category_id),
	FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
	FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS pending_notifications (
    notification_id SERIAL PRIMARY KEY,
    follower_user_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (follower_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);
