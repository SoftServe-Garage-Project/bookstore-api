-- V1__init_refactored.sql
-- Объединённая init-миграция: нормализованная схема с soft delete, рефакторингом payments и всеми изменениями.

-- Справочные таблицы (нормализация)
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

-- Основные таблицы
CREATE TABLE "user" (
                        id BIGSERIAL PRIMARY KEY,
                        username VARCHAR(100) NOT NULL,
                        email VARCHAR(150) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        role VARCHAR(50) NOT NULL,
                        balance NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
                        created_at TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP NOT NULL,
                        active BOOLEAN NOT NULL DEFAULT true,
                        deleted_at TIMESTAMP,
                        CONSTRAINT uq_user_email UNIQUE (email)
);

CREATE INDEX idx_user_email_active ON "user"(email, active);
CREATE INDEX idx_user_username_active ON "user"(username, active);
CREATE INDEX idx_user_role_active ON "user"(role, active);
CREATE INDEX idx_user_balance_active ON "user"(balance, active);
CREATE INDEX idx_user_role_created_desc ON "user"(role, created_at DESC);

CREATE TABLE book (
                      id BIGSERIAL PRIMARY KEY,
                      title VARCHAR(255) NOT NULL,
                      description TEXT,
                      published_year INTEGER NOT NULL,
                      price NUMERIC(10, 2) NOT NULL,
                      stock_quantity INTEGER NOT NULL,
                      discount_percentage NUMERIC(5, 2) DEFAULT 0.00,
                      page_count INTEGER,
                      cover_image_url VARCHAR(500),
                      genre_id BIGINT NOT NULL,
                      age_group_id BIGINT NOT NULL,
                      language_id BIGINT NOT NULL,
                      created_at TIMESTAMP NOT NULL,
                      updated_at TIMESTAMP NOT NULL,
                      active BOOLEAN NOT NULL DEFAULT true,
                      deleted_at TIMESTAMP,
                      CONSTRAINT chk_published_year CHECK (published_year >= 1000 AND published_year <= 2025),
                      CONSTRAINT chk_price CHECK (price >= 0.0),
                      CONSTRAINT chk_stock_quantity CHECK (stock_quantity >= 0),
                      CONSTRAINT chk_discount_percentage CHECK (discount_percentage >= 0.0 AND discount_percentage <= 100.0)
);

CREATE INDEX idx_book_title_active ON book(title, active);
CREATE INDEX idx_book_genre_active ON book(genre_id, active);
CREATE INDEX idx_book_price_range ON book(price);
CREATE INDEX idx_book_published_year_active ON book(published_year, active);
CREATE INDEX idx_book_language_age_active ON book(language_id, age_group_id, active);
CREATE INDEX idx_book_stock_discount_active ON book(stock_quantity, discount_percentage, active);
CREATE INDEX idx_book_page_count ON book(page_count);

-- Связующая таблица для авторов книг (Many-to-Many)
CREATE TABLE book_author (
                             book_id BIGINT NOT NULL,
                             author_id BIGINT NOT NULL,
                             PRIMARY KEY (book_id, author_id),
                             CONSTRAINT fk_book_author_book FOREIGN KEY (book_id) REFERENCES book(id),
                             CONSTRAINT fk_book_author_author FOREIGN KEY (author_id) REFERENCES author(id)
);

-- FK для book
ALTER TABLE book
    ADD CONSTRAINT fk_book_genre FOREIGN KEY (genre_id) REFERENCES genre(id),
    ADD CONSTRAINT fk_book_age_group FOREIGN KEY (age_group_id) REFERENCES age_group(id),
    ADD CONSTRAINT fk_book_language FOREIGN KEY (language_id) REFERENCES language(id);

-- Таблица разрешений пользователей
CREATE TABLE user_permissions (
                                  user_id BIGINT NOT NULL,
                                  permission VARCHAR(50),
                                  CONSTRAINT fk_user_permissions_user FOREIGN KEY (user_id) REFERENCES "user"(id)
);

-- Отзывы
CREATE TABLE review (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        book_id BIGINT NOT NULL,
                        rating INTEGER NOT NULL,
                        comment TEXT,
                        created_at TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP NOT NULL,
                        CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
                        CONSTRAINT fk_review_book FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE,
                        CONSTRAINT uq_review_user_book UNIQUE (user_id, book_id),
                        CONSTRAINT chk_rating CHECK (rating >= 1 AND rating <= 5)
);

CREATE INDEX idx_review_book_created_desc ON review(book_id, created_at DESC);
CREATE INDEX idx_review_user_created_desc ON review(user_id, created_at DESC);
CREATE INDEX idx_review_book_rating ON review(book_id, rating);
CREATE INDEX idx_review_rating_created ON review(rating, created_at);

-- Элементы корзины
CREATE TABLE cart_item (
                           id BIGSERIAL PRIMARY KEY,
                           user_id BIGINT NOT NULL,
                           book_id BIGINT NOT NULL,
                           quantity INTEGER NOT NULL DEFAULT 1,
                           CONSTRAINT fk_cart_item_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
                           CONSTRAINT fk_cart_item_book FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE,
                           CONSTRAINT uq_cart_item_user_book UNIQUE (user_id, book_id),
                           CONSTRAINT chk_quantity CHECK (quantity > 0)
);

-- Промокоды
CREATE TABLE promo_code (
                            id BIGSERIAL PRIMARY KEY,
                            code VARCHAR(20) NOT NULL UNIQUE,
                            discount_percentage NUMERIC(5, 2) NOT NULL,
                            valid_from TIMESTAMP NOT NULL,
                            valid_to TIMESTAMP NOT NULL,
                            description VARCHAR(255),
                            max_uses INTEGER,
                            current_uses INTEGER NOT NULL DEFAULT 0,
                            min_order_amount NUMERIC(10, 2) DEFAULT 0.00,
                            created_at TIMESTAMP NOT NULL,
                            updated_at TIMESTAMP NOT NULL,
                            active BOOLEAN NOT NULL DEFAULT true,
                            deleted_at TIMESTAMP,
                            CONSTRAINT chk_promo_discount CHECK (discount_percentage >= 0.0 AND discount_percentage <= 100.0),
                            CONSTRAINT chk_promo_dates CHECK (valid_to > valid_from)
);

CREATE INDEX idx_promo_code_active ON promo_code(code, active);
CREATE INDEX idx_promo_valid_dates_active ON promo_code(valid_from, valid_to, active);
CREATE INDEX idx_promo_min_amount_discount ON promo_code(min_order_amount, discount_percentage);
CREATE INDEX idx_promo_current_uses_active ON promo_code(current_uses, active);

-- Заказы
CREATE TABLE "order" (
                         id BIGSERIAL PRIMARY KEY,
                         user_id BIGINT NOT NULL,
                         status VARCHAR(50) NOT NULL,
                         total_amount NUMERIC(10, 2) NOT NULL,
                         promo_code_id BIGINT,
                         payment_method VARCHAR(20) NOT NULL,
                         created_at TIMESTAMP NOT NULL,
                         updated_at TIMESTAMP,
                         active BOOLEAN NOT NULL DEFAULT true,
                         deleted_at TIMESTAMP,
                         CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
                         CONSTRAINT fk_order_promo_code FOREIGN KEY (promo_code_id) REFERENCES promo_code(id) ON DELETE SET NULL,
                         CONSTRAINT chk_total_amount CHECK (total_amount >= 0.0)
);

CREATE INDEX idx_order_user_created_desc ON "order"(user_id, created_at DESC);
CREATE INDEX idx_order_status_active ON "order"(status, active);
CREATE INDEX idx_order_promo_active ON "order"(promo_code_id, active);
CREATE INDEX idx_order_total_amount_status ON "order"(total_amount, status);

-- Элементы заказа
CREATE TABLE order_item (
                            id BIGSERIAL PRIMARY KEY,
                            order_id BIGINT NOT NULL,
                            book_id BIGINT NOT NULL,
                            quantity INTEGER NOT NULL,
                            original_price NUMERIC(10, 2) NOT NULL,
                            book_discount_percentage NUMERIC(5, 2) DEFAULT 0.00,
                            promo_discount_percentage NUMERIC(5, 2) DEFAULT 0.00,
                            final_price NUMERIC(10, 2) NOT NULL,
                            CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES "order"(id) ON DELETE CASCADE,
                            CONSTRAINT fk_order_item_book FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE RESTRICT,
                            CONSTRAINT chk_order_quantity CHECK (quantity > 0),
                            CONSTRAINT chk_order_final_price CHECK (final_price >= 0.0)
);

-- Детали платежей
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
                                 description TEXT,
                                 CONSTRAINT fk_payment_details_user FOREIGN KEY (user_id) REFERENCES "user"(id)
);

-- Транзакции (рефакторинг payments)
CREATE TABLE transaction (
                             id BIGSERIAL PRIMARY KEY,
                             created_at TIMESTAMP NOT NULL,
                             updated_at TIMESTAMP,
                             sender_id BIGINT NOT NULL,
                             receiver_id BIGINT,
                             amount NUMERIC(10, 2) NOT NULL,
                             type VARCHAR(20) NOT NULL,
                             status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                             payment_method VARCHAR(20),
                             order_id BIGINT UNIQUE,
                             description TEXT,
                             payment_details_id BIGINT,
                             CONSTRAINT fk_transaction_sender FOREIGN KEY (sender_id) REFERENCES "user"(id) ON DELETE CASCADE,
                             CONSTRAINT fk_transaction_receiver FOREIGN KEY (receiver_id) REFERENCES "user"(id) ON DELETE CASCADE,
                             CONSTRAINT fk_transaction_order FOREIGN KEY (order_id) REFERENCES "order"(id),
                             CONSTRAINT fk_transaction_payment_details FOREIGN KEY (payment_details_id) REFERENCES payment_details(id),
                             CONSTRAINT chk_transaction_amount CHECK (amount >= 0.0)
);

CREATE INDEX idx_transaction_sender_created_desc ON transaction(sender_id, created_at DESC);
CREATE INDEX idx_transaction_receiver_created_desc ON transaction(receiver_id, created_at DESC);
CREATE INDEX idx_transaction_type_status_created ON transaction(type, status, created_at DESC);
CREATE INDEX idx_transaction_order_id ON transaction(order_id);
CREATE INDEX idx_transaction_payment_method_status ON transaction(payment_method, status);
CREATE INDEX idx_transaction_amount_type ON transaction(amount, type);

