CREATE TABLE IF NOT EXISTS "users" (
	"user_id" SERIAL PRIMARY KEY,
	"email" VARCHAR(255) NOT NULL UNIQUE,
	"password" VARCHAR(255),
	"mod" BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS "products" (
	"product_id" SERIAL PRIMARY KEY,
	"usr_id" INTEGER NOT NULL UNIQUE,
	"name" VARCHAR(255) NOT NULL,
	"published" DATE NOT NULL,
	"condition" VARCHAR(255)NOT NULL,
	"price" MONEY NOT NULL
);

CREATE TABLE IF NOT EXISTS "images" (
	"product_id" INTEGER NOT NULL,
	"data" BYTEA NOT NULL,
	PRIMARY KEY("product_id", "data")
);

CREATE TABLE IF NOT EXISTS "categories" (
	"category_id" SERIAL PRIMARY KEY,
	"name" TEXT NOT NULL
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
	"payment_method" TEXT NOT NULL,
	"confirmed" BOOLEAN NOT NULL,
	PRIMARY KEY("product_id", "user_id")
);

ALTER TABLE "products"
ADD FOREIGN KEY("usr_id") REFERENCES "users"("user_id")
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