-- Users table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(100) NOT NULL,
                       email VARCHAR(150) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       balance NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email ON users(email);

-- Books table
CREATE TABLE books (
                       id BIGSERIAL PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       author VARCHAR(255) NOT NULL,
                       description TEXT,
                       genre VARCHAR(50) NOT NULL,
                       age_group VARCHAR(50) NOT NULL,
                       published_year INTEGER NOT NULL,
                       language VARCHAR(50) NOT NULL,
                       price NUMERIC(10, 2) NOT NULL,
                       stock_quantity INTEGER NOT NULL,
                       discount_percentage NUMERIC(5, 2) DEFAULT 0.00,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT chk_published_year CHECK (published_year >= 1000 AND published_year <= 2025),
                       CONSTRAINT chk_price CHECK (price >= 0.0),
                       CONSTRAINT chk_stock_quantity CHECK (stock_quantity >= 0),
                       CONSTRAINT chk_discount_percentage CHECK (discount_percentage >= 0.0 AND discount_percentage <= 100.0)
);

-- Reviews table
CREATE TABLE reviews (
                         id BIGSERIAL PRIMARY KEY,
                         user_id BIGINT NOT NULL,
                         book_id BIGINT NOT NULL,
                         rating INTEGER NOT NULL,
                         comment TEXT,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                         CONSTRAINT fk_reviews_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                         CONSTRAINT uq_reviews_user_book UNIQUE (user_id, book_id),
                         CONSTRAINT chk_rating CHECK (rating >= 1 AND rating <= 5)
);

-- Cart items table
CREATE TABLE cart_items (
                            id BIGSERIAL PRIMARY KEY,
                            user_id BIGINT NOT NULL,
                            book_id BIGINT NOT NULL,
                            quantity INTEGER NOT NULL DEFAULT 1,
                            CONSTRAINT fk_cart_items_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT fk_cart_items_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                            CONSTRAINT uq_cart_items_user_book UNIQUE (user_id, book_id),
                            CONSTRAINT chk_quantity CHECK (quantity > 0)
);

-- Promo codes table
CREATE TABLE promocodes (
                            id BIGSERIAL PRIMARY KEY,
                            code VARCHAR(50) NOT NULL UNIQUE,
                            discount_percentage NUMERIC(5, 2) NOT NULL,
                            start_date TIMESTAMP NOT NULL,
                            end_date TIMESTAMP NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT chk_promo_discount CHECK (discount_percentage >= 0.0 AND discount_percentage <= 100.0),
                            CONSTRAINT chk_promo_dates CHECK (end_date > start_date)
);

-- Orders table
CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        total_amount NUMERIC(10, 2) NOT NULL,
                        promo_code_id BIGINT,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                        CONSTRAINT fk_orders_promo_code FOREIGN KEY (promo_code_id) REFERENCES promocodes(id) ON DELETE SET NULL,
                        CONSTRAINT chk_total_amount CHECK (total_amount >= 0.0)
);

-- Order items table
CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             book_id BIGINT NOT NULL,
                             quantity INTEGER NOT NULL,
                             price NUMERIC(10, 2) NOT NULL,
                             CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                             CONSTRAINT fk_order_items_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE RESTRICT,
                             CONSTRAINT chk_order_quantity CHECK (quantity > 0),
                             CONSTRAINT chk_order_price CHECK (price >= 0.0)
);

-- Payments table
CREATE TABLE payments (
                          id BIGSERIAL PRIMARY KEY,
                          payer_email VARCHAR(150) NOT NULL,
                          receiver_email VARCHAR(150) NOT NULL,
                          amount NUMERIC(10, 2) NOT NULL,
                          status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                          method VARCHAR(20),
                          card_number VARCHAR(255),
                          pay_pal_email VARCHAR(255),
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_payments_payer FOREIGN KEY (payer_email) REFERENCES users(email) ON DELETE CASCADE,
                          CONSTRAINT fk_payments_receiver FOREIGN KEY (receiver_email) REFERENCES users(email) ON DELETE CASCADE,
                          CONSTRAINT chk_payment_amount CHECK (amount >= 0.0)
);

-- Indexes for performance
CREATE INDEX idx_books_genre ON books(genre);
CREATE INDEX idx_books_language ON books(language);
CREATE INDEX idx_books_age_group ON books(age_group);
CREATE INDEX idx_reviews_book_id ON reviews(book_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_payments_payer_email ON payments(payer_email);
CREATE INDEX idx_payments_receiver_email ON payments(receiver_email);
