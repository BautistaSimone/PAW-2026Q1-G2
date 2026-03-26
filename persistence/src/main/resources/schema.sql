CREATE TABLE IF NOT EXISTS "users" (
	"user_id" SERIAL PRIMARY KEY,
	"email" VARCHAR(255) NOT NULL UNIQUE,
	"password" VARCHAR(255),
	"username" VARCHAR(255) NOT NULL,
	"mod" BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS "products" (
	"product_id" SERIAL PRIMARY KEY,
	"user_id" INTEGER NOT NULL UNIQUE,
	"title" VARCHAR(255) NOT NULL,
	"artist" VARCHAR(255) NOT NULL,
	"genre" VARCHAR(255) NOT NULL,
	"description" TEXT NOT NULL,
	"condition" VARCHAR(255) NOT NULL,
	"published" DATE NOT NULL,
	"price" NUMERIC NOT NULL
);

CREATE TABLE IF NOT EXISTS "images" (
	"product_id" INTEGER NOT NULL,
	"data" BYTEA NOT NULL,
	PRIMARY KEY("product_id", "data")
);

CREATE TABLE IF NOT EXISTS "categories" (
	"category_id" SERIAL PRIMARY KEY,
	"name" VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS "products_categories" (
	"product_id" INTEGER NOT NULL,
	"category_id" INTEGER NOT NULL,
	PRIMARY KEY("product_id", "category_id")
);

CREATE TABLE IF NOT EXISTS "purchases" (
	"product_id" INTEGER NOT NULL,
	"user_id" INTEGER NOT NULL,
	"date" DATE NOT NULL,
	"payment_method" VARCHAR(255) NOT NULL,
	"confirmed" BOOLEAN NOT NULL,
	PRIMARY KEY("product_id", "user_id")
);

ALTER TABLE "products"
ADD FOREIGN KEY("user_id") REFERENCES "users"("user_id")
ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "images"
ADD FOREIGN KEY("product_id") REFERENCES "products"("product_id")
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE "products_categories"
ADD FOREIGN KEY("category_id") REFERENCES "categories"("category_id")
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE "products_categories"
ADD FOREIGN KEY("product_id") REFERENCES "products"("product_id")
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE "purchases"
ADD FOREIGN KEY("product_id") REFERENCES "products"("product_id")
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE "purchases"
ADD FOREIGN KEY("user_id") REFERENCES "users"("user_id")
ON UPDATE NO ACTION ON DELETE NO ACTION;