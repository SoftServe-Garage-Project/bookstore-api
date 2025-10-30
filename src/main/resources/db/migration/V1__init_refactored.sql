-- V1__init_refactored.sql

CREATE SCHEMA IF NOT EXISTS public;

CREATE TABLE public.age_group (
                                  id BIGSERIAL PRIMARY KEY,
                                  created_at TIMESTAMP NOT NULL,
                                  updated_at TIMESTAMP,
                                  is_active BOOLEAN NOT NULL DEFAULT true,
                                  name VARCHAR(50) NOT NULL UNIQUE,
                                  description TEXT,
                                  min_age INTEGER CHECK (min_age >= 0),
                                  max_age INTEGER CHECK (max_age >= 1 AND max_age <= 120)
);

CREATE TABLE public.genre (
                              id BIGSERIAL PRIMARY KEY,
                              created_at TIMESTAMP NOT NULL,
                              updated_at TIMESTAMP,
                              is_active BOOLEAN NOT NULL DEFAULT true,
                              name VARCHAR(100) NOT NULL UNIQUE,
                              description TEXT
);

CREATE TABLE public.language (
                                 id BIGSERIAL PRIMARY KEY,
                                 created_at TIMESTAMP NOT NULL,
                                 updated_at TIMESTAMP,
                                 is_active BOOLEAN NOT NULL DEFAULT true,
                                 code VARCHAR(10) NOT NULL UNIQUE,
                                 name VARCHAR(100) NOT NULL
);

CREATE TABLE public.author (
                               id BIGSERIAL PRIMARY KEY,
                               created_at TIMESTAMP NOT NULL,
                               updated_at TIMESTAMP,
                               is_active BOOLEAN NOT NULL DEFAULT true,
                               first_name VARCHAR(100) NOT NULL,
                               last_name VARCHAR(100) NOT NULL,
                               biography TEXT,
                               photo_url VARCHAR(500),
                               country VARCHAR(100)
);

CREATE INDEX idx_author_name_composite ON public.author (last_name, first_name);

CREATE TABLE public.accounts (
                                 id BIGSERIAL PRIMARY KEY,
                                 username VARCHAR(100) NOT NULL,
                                 email VARCHAR(150) NOT NULL,
                                 password VARCHAR(255) NOT NULL,
                                 role VARCHAR(50) NOT NULL DEFAULT 'ROLE_CUSTOMER',
                                 balance NUMERIC(10, 2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0.00),
                                 created_at TIMESTAMP NOT NULL,
                                 updated_at TIMESTAMP,
                                 is_active BOOLEAN NOT NULL DEFAULT true
);

CREATE UNIQUE INDEX uq_accounts_email_active ON public.accounts (email) WHERE is_active = true;

CREATE INDEX idx_accounts_email_active ON public.accounts (email, is_active);

CREATE TABLE public.book (
                             id BIGSERIAL PRIMARY KEY,
                             title VARCHAR(255) NOT NULL,
                             description TEXT,
                             published_year INTEGER NOT NULL CHECK (published_year >= 1000 AND published_year <= 2100),
                             price NUMERIC(10, 2) NOT NULL CHECK (price >= 0.0),
                             stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),
                             discount_percentage NUMERIC(5, 2) DEFAULT 0.00 CHECK (discount_percentage >= 0.0 AND discount_percentage <= 100.0),
                             page_count INTEGER CHECK (page_count >= 1),
                             cover_image_url VARCHAR(500),
                             genre_id BIGINT NOT NULL,
                             age_group_id BIGINT NOT NULL,
                             language_id BIGINT NOT NULL,
                             created_at TIMESTAMP NOT NULL,
                             updated_at TIMESTAMP,
                             is_active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_books_title_active ON public.book (title, is_active);
CREATE INDEX idx_books_genre_active ON public.book (genre_id, is_active);
CREATE INDEX idx_books_price ON public.book (price);

ALTER TABLE public.book
    ADD CONSTRAINT fk_book_genre FOREIGN KEY (genre_id) REFERENCES public.genre(id),
    ADD CONSTRAINT fk_book_age_group FOREIGN KEY (age_group_id) REFERENCES public.age_group(id),
    ADD CONSTRAINT fk_book_language FOREIGN KEY (language_id) REFERENCES public.language(id);

CREATE TABLE public.book_author (
                                    book_id BIGINT NOT NULL,
                                    author_id BIGINT NOT NULL,
                                    PRIMARY KEY (book_id, author_id),
                                    CONSTRAINT fk_book_author_book FOREIGN KEY (book_id) REFERENCES public.book(id) ON DELETE CASCADE,
                                    CONSTRAINT fk_book_author_author FOREIGN KEY (author_id) REFERENCES public.author(id) ON DELETE CASCADE
);

CREATE TABLE public.account_permissions (
                                            account_id BIGINT NOT NULL,
                                            permission VARCHAR(50) NOT NULL,
                                            PRIMARY KEY (account_id, permission),
                                            CONSTRAINT fk_account_permissions_account FOREIGN KEY (account_id) REFERENCES public.accounts(id) ON DELETE CASCADE
);

CREATE TABLE public.review (
                               id BIGSERIAL PRIMARY KEY,
                               account_id BIGINT NOT NULL,
                               book_id BIGINT NOT NULL,
                               rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
                               comment TEXT,
                               created_at TIMESTAMP NOT NULL,
                               updated_at TIMESTAMP,
                               CONSTRAINT fk_review_account FOREIGN KEY (account_id) REFERENCES public.accounts(id) ON DELETE CASCADE,
                               CONSTRAINT fk_review_book FOREIGN KEY (book_id) REFERENCES public.book(id) ON DELETE CASCADE,
                               CONSTRAINT uq_review_account_book UNIQUE (account_id, book_id)
);

CREATE INDEX idx_review_book_created_desc ON public.review (book_id, created_at DESC);

CREATE TABLE public.cart_item (
                                  id BIGSERIAL PRIMARY KEY,
                                  account_id BIGINT NOT NULL,
                                  book_id BIGINT NOT NULL,
                                  quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity >= 1),
                                  CONSTRAINT fk_cart_item_account FOREIGN KEY (account_id) REFERENCES public.accounts(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_cart_item_book FOREIGN KEY (book_id) REFERENCES public.book(id) ON DELETE CASCADE,
                                  CONSTRAINT uq_cart_item_account_book UNIQUE (account_id, book_id)
);

CREATE INDEX idx_cart_items_account ON public.cart_item (account_id);

CREATE TABLE public.promo_code (
                                   id BIGSERIAL PRIMARY KEY,
                                   code VARCHAR(20) NOT NULL,
                                   discount_percentage NUMERIC(5, 2) NOT NULL CHECK (discount_percentage > 0.0 AND discount_percentage < 100.0),
                                   description VARCHAR(255),
                                   valid_from TIMESTAMP NOT NULL,
                                   valid_to TIMESTAMP,
                                   max_uses INTEGER CHECK (max_uses >= 0),
                                   current_uses INTEGER NOT NULL DEFAULT 0 CHECK (current_uses >= 0),
                                   min_order_amount NUMERIC(10, 2) DEFAULT 0.00 CHECK (min_order_amount >= 0.0),
                                   created_at TIMESTAMP NOT NULL,
                                   updated_at TIMESTAMP,
                                   is_active BOOLEAN NOT NULL DEFAULT true,
                                   CONSTRAINT chk_promo_dates CHECK (valid_to IS NULL OR valid_to > valid_from),
                                   CONSTRAINT chk_promo_uses CHECK (max_uses IS NULL OR current_uses <= max_uses)
);

-- Partial unique index for promo_code.code (enforces unique only for active)
CREATE UNIQUE INDEX uq_promo_code_active ON public.promo_code (code) WHERE is_active = true;
-- No ADD CONSTRAINT — partial index self-enforces

CREATE INDEX idx_promo_codes_code_active ON public.promo_code (code, is_active);

CREATE TABLE public.orders (
                               id BIGSERIAL PRIMARY KEY,
                               account_id BIGINT NOT NULL,
                               status VARCHAR(50) NOT NULL,
                               total_amount NUMERIC(10, 2) NOT NULL CHECK (total_amount >= 0.0),
                               promo_code_id BIGINT,
                               payment_method VARCHAR(20) NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               updated_at TIMESTAMP,
                               is_active BOOLEAN NOT NULL DEFAULT true,
                               CONSTRAINT fk_orders_account FOREIGN KEY (account_id) REFERENCES public.accounts(id) ON DELETE CASCADE,
                               CONSTRAINT fk_orders_promo_code FOREIGN KEY (promo_code_id) REFERENCES public.promo_code(id) ON DELETE SET NULL
);

CREATE INDEX idx_orders_account_created_desc ON public.orders (account_id, created_at DESC);
CREATE INDEX idx_orders_status_active ON public.orders (status, is_active);

CREATE TABLE public.order_item (
                                   id BIGSERIAL PRIMARY KEY,
                                   order_id BIGINT NOT NULL,
                                   book_id BIGINT NOT NULL,
                                   quantity INTEGER NOT NULL CHECK (quantity >= 1),
                                   original_price NUMERIC(10, 2) NOT NULL CHECK (original_price >= 0.0),
                                   book_discount_percentage NUMERIC(5, 2) DEFAULT 0.00 CHECK (book_discount_percentage >= 0.0 AND book_discount_percentage <= 100.0),
                                   promo_discount_percentage NUMERIC(5, 2) DEFAULT 0.00 CHECK (promo_discount_percentage >= 0.0 AND promo_discount_percentage <= 100.0),
                                   final_price NUMERIC(10, 2) NOT NULL CHECK (final_price >= 0.0),
                                   CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES public.orders(id) ON DELETE CASCADE,
                                   CONSTRAINT fk_order_item_book FOREIGN KEY (book_id) REFERENCES public.book(id) ON DELETE RESTRICT
);

CREATE TABLE public.payment_details (
                                        id BIGSERIAL PRIMARY KEY,
                                        account_id BIGINT NOT NULL,
                                        payment_method VARCHAR(20) NOT NULL,
                                        card_number VARCHAR(20),
                                        card_holder_name VARCHAR(100),
                                        card_expiry VARCHAR(7),
                                        paypal_email VARCHAR(150),
                                        description TEXT,
                                        created_at TIMESTAMP NOT NULL,
                                        updated_at TIMESTAMP,
                                        CONSTRAINT fk_payment_details_account FOREIGN KEY (account_id) REFERENCES public.accounts(id) ON DELETE CASCADE
);

CREATE TABLE public.transaction (
                                    id BIGSERIAL PRIMARY KEY,
                                    sender_account_id BIGINT NOT NULL,
                                    receiver_account_id BIGINT,
                                    amount NUMERIC(10, 2) NOT NULL CHECK (amount >= 0.0),
                                    type VARCHAR(20) NOT NULL,
                                    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                    payment_method VARCHAR(20),
                                    order_id BIGINT,
                                    description TEXT,
                                    payment_details_id BIGINT,
                                    created_at TIMESTAMP NOT NULL,
                                    updated_at TIMESTAMP,
                                    CONSTRAINT fk_transaction_sender_account FOREIGN KEY (sender_account_id) REFERENCES public.accounts(id) ON DELETE CASCADE,
                                    CONSTRAINT fk_transaction_receiver_account FOREIGN KEY (receiver_account_id) REFERENCES public.accounts(id) ON DELETE SET NULL,
                                    CONSTRAINT fk_transaction_order FOREIGN KEY (order_id) REFERENCES public.orders(id) ON DELETE SET NULL,
                                    CONSTRAINT fk_transaction_payment_details FOREIGN KEY (payment_details_id) REFERENCES public.payment_details(id) ON DELETE SET NULL
);

CREATE INDEX idx_transactions_sender_created_desc ON public.transaction (sender_account_id, created_at DESC);
