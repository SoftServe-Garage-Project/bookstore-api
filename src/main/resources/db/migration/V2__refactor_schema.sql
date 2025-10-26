-- V2__refactor_schema.sql
-- Масштабный рефакторинг схемы:
-- 1. Внедрение SoftDeletableEntity (поля 'active', 'deleted_at').
-- 2. Нормализация таблиц (Author, Genre, Language, AgeGroup).
-- 3. Рефакторинг Payments -> Transaction + PaymentDetails.
-- 4. Переименование таблиц (во множественном числе -> в единственное).
-- 5. Обновление всех индексов и ограничений в соответствии с JPA-сущностями.

CREATE TABLE age_group (
                           id BIGSERIAL PRIMARY KEY,
                           created_at TIMESTAMP NOT NULL,
                           updated_at TIMESTAMP,
                           active BOOLEAN NOT NULL DEFAULT true,
                           deleted_at TIMESTAMP,
                           name VARCHAR(50) NOT NULL UNIQUE,
                           description TEXT,
                           min_age INTEGER,
                           max_age INTEGER
);
CREATE INDEX idx_age_group_name ON age_group(name);
CREATE INDEX idx_age_group_active ON age_group(active);

CREATE TABLE genre (
                       id BIGSERIAL PRIMARY KEY,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP,
                       active BOOLEAN NOT NULL DEFAULT true,
                       deleted_at TIMESTAMP,
                       name VARCHAR(100) NOT NULL UNIQUE,
                       description TEXT
);
CREATE INDEX idx_genre_name ON genre(name);
CREATE INDEX idx_genre_active ON genre(active);

CREATE TABLE language (
                          id BIGSERIAL PRIMARY KEY,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP,
                          active BOOLEAN NOT NULL DEFAULT true,
                          deleted_at TIMESTAMP,
                          code VARCHAR(10) NOT NULL UNIQUE,
                          name VARCHAR(100) NOT NULL
);
CREATE INDEX idx_language_code ON language(code);
CREATE INDEX idx_language_active ON language(active);

CREATE TABLE author (
                        id BIGSERIAL PRIMARY KEY,
                        created_at TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP,
                        active BOOLEAN NOT NULL DEFAULT true,
                        deleted_at TIMESTAMP,
                        first_name VARCHAR(100) NOT NULL,
                        last_name VARCHAR(100) NOT NULL,
                        biography TEXT,
                        photo_url VARCHAR(500),
                        country VARCHAR(100)
);
CREATE INDEX idx_author_name_composite ON author(last_name, first_name);
CREATE INDEX idx_author_country_active ON author(country, active);
CREATE INDEX idx_author_active ON author(active);

CREATE TABLE book_author (
                             book_id BIGINT NOT NULL,
                             author_id BIGINT NOT NULL,
                             PRIMARY KEY (book_id, author_id),
                             CONSTRAINT fk_book_author_book FOREIGN KEY (book_id) REFERENCES books(id),
                             CONSTRAINT fk_book_author_author FOREIGN KEY (author_id) REFERENCES author(id)
);

CREATE TABLE payment_details (
                                 id BIGSERIAL PRIMARY KEY,
                                 created_at TIMESTAMP NOT NULL,
                                 updated_at TIMESTAMP,
                                 user_id BIGINT NOT NULL,
                                 payment_method VARCHAR(20) NOT NULL,
                                 card_number VARCHAR(20),
                                 card_holder_name VARCHAR(100),
                                 card_expiry VARCHAR(7),
                                 paypal_email VARCHAR(150),
                                 description TEXT
);

CREATE TABLE transaction (
                             id BIGSERIAL PRIMARY KEY,
                             created_at TIMESTAMP NOT NULL,
                             updated_at TIMESTAMP,
                             sender_id BIGINT NOT NULL,
                             receiver_id BIGINT,
                             amount NUMERIC(10, 2) NOT NULL,
                             type VARCHAR(20) NOT NULL,
                             status VARCHAR(20) NOT NULL,
                             payment_method VARCHAR(20),
                             order_id BIGINT UNIQUE,
                             description TEXT,
                             payment_details_id BIGINT,
                             CONSTRAINT fk_transaction_payment_details FOREIGN KEY (payment_details_id) REFERENCES payment_details(id)
);
CREATE INDEX idx_transaction_sender_created_desc ON transaction(sender_id, created_at DESC);
CREATE INDEX idx_transaction_receiver_created_desc ON transaction(receiver_id, created_at DESC);
CREATE INDEX idx_transaction_type_status_created ON transaction(type, status, created_at DESC);
CREATE INDEX idx_transaction_order_id ON transaction(order_id);
CREATE INDEX idx_transaction_payment_method_status ON transaction(payment_method, status);
CREATE INDEX idx_transaction_amount_type ON transaction(amount, type);

ALTER TABLE users RENAME TO "user";
ALTER SEQUENCE users_id_seq RENAME TO user_id_seq;
ALTER TABLE "user" RENAME CONSTRAINT users_pkey TO user_pkey;
ALTER TABLE "user" RENAME CONSTRAINT uq_users_email TO uq_user_email;
DROP INDEX idx_users_email;

ALTER TABLE "user" ALTER COLUMN created_at DROP DEFAULT;
ALTER TABLE "user" ALTER COLUMN updated_at DROP DEFAULT;

ALTER TABLE "user" ADD COLUMN active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE "user" ADD COLUMN deleted_at TIMESTAMP;

CREATE TABLE user_permissions (
                                  user_id BIGINT NOT NULL,
                                  permission VARCHAR(50),
                                  CONSTRAINT fk_user_permissions_user FOREIGN KEY (user_id) REFERENCES "user"(id)
);

CREATE INDEX idx_user_email_active ON "user"(email, active);
CREATE INDEX idx_user_username_active ON "user"(username, active);
CREATE INDEX idx_user_role_active ON "user"(role, active);
CREATE INDEX idx_user_balance_active ON "user"(balance, active);
CREATE INDEX idx_user_role_created_desc ON "user"(role, created_at DESC);

ALTER TABLE books RENAME TO book;
ALTER SEQUENCE books_id_seq RENAME TO book_id_seq;
ALTER TABLE book RENAME CONSTRAINT books_pkey TO book_pkey;

DROP INDEX idx_books_genre;
DROP INDEX idx_books_language;
DROP INDEX idx_books_age_group;

ALTER TABLE book DROP CONSTRAINT chk_published_year;
ALTER TABLE book DROP CONSTRAINT chk_price;
ALTER TABLE book DROP CONSTRAINT chk_stock_quantity;
ALTER TABLE book DROP CONSTRAINT chk_discount_percentage;

ALTER TABLE book ALTER COLUMN created_at DROP DEFAULT;
ALTER TABLE book ALTER COLUMN updated_at DROP DEFAULT;

ALTER TABLE book ADD COLUMN active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE book ADD COLUMN deleted_at TIMESTAMP;

ALTER TABLE book ADD COLUMN page_count INTEGER;
ALTER TABLE book ADD COLUMN cover_image_url VARCHAR(500);

ALTER TABLE book ADD COLUMN genre_id BIGINT NOT NULL;
ALTER TABLE book ADD COLUMN age_group_id BIGINT NOT NULL;
ALTER TABLE book ADD COLUMN language_id BIGINT NOT NULL;

ALTER TABLE book ADD CONSTRAINT fk_book_genre FOREIGN KEY (genre_id) REFERENCES genre(id);
ALTER TABLE book ADD CONSTRAINT fk_book_age_group FOREIGN KEY (age_group_id) REFERENCES age_group(id);
ALTER TABLE book ADD CONSTRAINT fk_book_language FOREIGN KEY (language_id) REFERENCES language(id);

ALTER TABLE book DROP COLUMN author;
ALTER TABLE book DROP COLUMN genre;
ALTER TABLE book DROP COLUMN age_group;
ALTER TABLE book DROP COLUMN language;

CREATE INDEX idx_book_title_active ON book(title, active);
CREATE INDEX idx_book_genre_active ON book(genre_id, active);
CREATE INDEX idx_book_price_range ON book(price);
CREATE INDEX idx_book_published_year_active ON book(published_year, active);
CREATE INDEX idx_book_language_age_active ON book(language_id, age_group_id, active);
CREATE INDEX idx_book_stock_discount_active ON book(stock_quantity, discount_percentage, active);
CREATE INDEX idx_book_page_count ON book(page_count);

ALTER TABLE reviews RENAME TO review;
ALTER SEQUENCE reviews_id_seq RENAME TO review_id_seq;
ALTER TABLE review RENAME CONSTRAINT reviews_pkey TO review_pkey;
ALTER TABLE review RENAME CONSTRAINT uq_reviews_user_book TO uq_review_user_book;
ALTER TABLE review DROP CONSTRAINT chk_rating;

ALTER TABLE review ALTER COLUMN created_at DROP DEFAULT;
ALTER TABLE review ALTER COLUMN updated_at DROP DEFAULT;

DROP INDEX idx_reviews_book_id;
DROP INDEX idx_reviews_user_id;

ALTER TABLE review DROP CONSTRAINT fk_reviews_user;
ALTER TABLE review ADD CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES "user"(id);

ALTER TABLE review DROP CONSTRAINT fk_reviews_book;
ALTER TABLE review ADD CONSTRAINT fk_review_book FOREIGN KEY (book_id) REFERENCES book(id);

CREATE INDEX idx_review_book_created_desc ON review(book_id, created_at DESC);
CREATE INDEX idx_review_user_created_desc ON review(user_id, created_at DESC);
CREATE INDEX idx_review_book_rating ON review(book_id, rating);
CREATE INDEX idx_review_rating_created ON review(rating, created_at);

ALTER TABLE cart_items RENAME TO cart_item;
ALTER SEQUENCE cart_items_id_seq RENAME TO cart_item_id_seq;
ALTER TABLE cart_item RENAME CONSTRAINT cart_items_pkey TO cart_item_pkey;
ALTER TABLE cart_item RENAME CONSTRAINT uq_cart_items_user_book TO uq_cart_item_user_book;
ALTER TABLE cart_item DROP CONSTRAINT chk_quantity;

ALTER TABLE cart_item DROP CONSTRAINT fk_cart_items_user;
ALTER TABLE cart_item ADD CONSTRAINT fk_cart_item_user FOREIGN KEY (user_id) REFERENCES "user"(id);

ALTER TABLE cart_item DROP CONSTRAINT fk_cart_items_book;
ALTER TABLE cart_item ADD CONSTRAINT fk_cart_item_book FOREIGN KEY (book_id) REFERENCES book(id);

ALTER TABLE promocodes RENAME TO promo_code;
ALTER SEQUENCE promocodes_id_seq RENAME TO promo_code_id_seq;
ALTER TABLE promo_code RENAME CONSTRAINT promocodes_pkey TO promo_code_pkey;
ALTER TABLE promo_code RENAME CONSTRAINT promocodes_code_key TO uq_promo_code;

ALTER TABLE promo_code DROP CONSTRAINT chk_promo_discount;
ALTER TABLE promo_code DROP CONSTRAINT chk_promo_dates;

ALTER TABLE promo_code ALTER COLUMN created_at DROP DEFAULT;
ALTER TABLE promo_code ALTER COLUMN updated_at DROP DEFAULT;

ALTER TABLE promo_code RENAME COLUMN start_date TO valid_from;
ALTER TABLE promo_code RENAME COLUMN end_date TO valid_to;

ALTER TABLE promo_code ALTER COLUMN code TYPE VARCHAR(20);

ALTER TABLE promo_code ADD COLUMN active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE promo_code ADD COLUMN deleted_at TIMESTAMP;

ALTER TABLE promo_code ADD COLUMN description VARCHAR(255);
ALTER TABLE promo_code ADD COLUMN max_uses INTEGER;
ALTER TABLE promo_code ADD COLUMN current_uses INTEGER NOT NULL DEFAULT 0;
ALTER TABLE promo_code ADD COLUMN min_order_amount NUMERIC(10, 2) DEFAULT 0.00;

CREATE INDEX idx_promo_code_active ON promo_code(code, active);
CREATE INDEX idx_promo_valid_dates_active ON promo_code(valid_from, valid_to, active);
CREATE INDEX idx_promo_min_amount_discount ON promo_code(min_order_amount, discount_percentage);
CREATE INDEX idx_promo_current_uses_active ON promo_code(current_uses, active);

ALTER TABLE orders RENAME TO "order";
ALTER SEQUENCE orders_id_seq RENAME TO order_id_seq;
ALTER TABLE "order" RENAME CONSTRAINT orders_pkey TO order_pkey;
ALTER TABLE "order" DROP CONSTRAINT chk_total_amount;

ALTER TABLE "order" ALTER COLUMN created_at DROP DEFAULT;

ALTER TABLE "order" ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE "order" ADD COLUMN active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE "order" ADD COLUMN deleted_at TIMESTAMP;

ALTER TABLE "order" ADD COLUMN payment_method VARCHAR(20) NOT NULL;

DROP INDEX idx_orders_user_id;
DROP INDEX idx_orders_status;

ALTER TABLE "order" DROP CONSTRAINT fk_orders_user;
ALTER TABLE "order" ADD CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES "user"(id);

ALTER TABLE "order" DROP CONSTRAINT fk_orders_promo_code;
ALTER TABLE "order" ADD CONSTRAINT fk_order_promo_code FOREIGN KEY (promo_code_id) REFERENCES promo_code(id);

CREATE INDEX idx_order_user_created_desc ON "order"(user_id, created_at DESC);
CREATE INDEX idx_order_status_active ON "order"(status, active);
CREATE INDEX idx_order_promo_active ON "order"(promo_code_id, active);
CREATE INDEX idx_order_total_amount_status ON "order"(total_amount, status);

ALTER TABLE order_items RENAME TO order_item;
ALTER SEQUENCE order_items_id_seq RENAME TO order_item_id_seq;
ALTER TABLE order_item RENAME CONSTRAINT order_items_pkey TO order_item_pkey;

ALTER TABLE order_item DROP CONSTRAINT chk_order_quantity;
ALTER TABLE order_item DROP CONSTRAINT chk_order_price;
DROP INDEX idx_order_items_order_id;

ALTER TABLE order_item DROP CONSTRAINT fk_order_items_order;
ALTER TABLE order_item ADD CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES "order"(id);

ALTER TABLE order_item DROP CONSTRAINT fk_order_items_book;
ALTER TABLE order_item ADD CONSTRAINT fk_order_item_book FOREIGN KEY (book_id) REFERENCES book(id);

ALTER TABLE order_item ADD COLUMN original_price NUMERIC(10, 2) NOT NULL;
ALTER TABLE order_item ADD COLUMN book_discount_percentage NUMERIC(5, 2) DEFAULT 0.00;
ALTER TABLE order_item ADD COLUMN promo_discount_percentage NUMERIC(5, 2) DEFAULT 0.00;
ALTER TABLE order_item ADD COLUMN final_price NUMERIC(10, 2) NOT NULL;

ALTER TABLE order_item DROP COLUMN price;

DROP TABLE payments;

ALTER TABLE payment_details
    ADD CONSTRAINT fk_payment_details_user FOREIGN KEY (user_id) REFERENCES "user"(id);

ALTER TABLE transaction
    ADD CONSTRAINT fk_transaction_sender FOREIGN KEY (sender_id) REFERENCES "user"(id),
    ADD CONSTRAINT fk_transaction_receiver FOREIGN KEY (receiver_id) REFERENCES "user"(id),
    ADD CONSTRAINT fk_transaction_order FOREIGN KEY (order_id) REFERENCES "order"(id);

ALTER TABLE book_author DROP CONSTRAINT fk_book_author_book;
ALTER TABLE book_author ADD CONSTRAINT fk_book_author_book FOREIGN KEY (book_id) REFERENCES book(id);