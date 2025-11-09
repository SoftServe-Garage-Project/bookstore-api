-- V2__refactor_table_names_and_structure.sql
-- Migration to rename tables to plural form and update relationships

-- 1. Rename transaction table to transactions
ALTER TABLE IF EXISTS public.transaction RENAME TO transactions;

-- 2. Rename age_group to age_groups and remove soft delete fields
ALTER TABLE IF EXISTS public.age_group RENAME TO age_groups;
ALTER TABLE IF EXISTS public.age_groups DROP COLUMN IF EXISTS is_active;
ALTER TABLE IF EXISTS public.age_groups DROP COLUMN IF EXISTS updated_at;

-- 3. Rename genre to genres and remove soft delete fields
ALTER TABLE IF EXISTS public.genre RENAME TO genres;
ALTER TABLE IF EXISTS public.genres DROP COLUMN IF EXISTS is_active;
ALTER TABLE IF EXISTS public.genres DROP COLUMN IF EXISTS updated_at;

-- 4. Rename language to languages and remove soft delete fields
ALTER TABLE IF EXISTS public.language RENAME TO languages;
ALTER TABLE IF EXISTS public.languages DROP COLUMN IF EXISTS is_active;
ALTER TABLE IF EXISTS public.languages DROP COLUMN IF EXISTS updated_at;

-- 5. Rename author to authors
ALTER TABLE IF EXISTS public.author RENAME TO authors;

-- 6. Rename book to books
ALTER TABLE IF EXISTS public.book RENAME TO books;

-- 7. Drop old foreign key constraints from books
ALTER TABLE IF EXISTS public.books DROP CONSTRAINT IF EXISTS fk_book_genre;
ALTER TABLE IF EXISTS public.books DROP CONSTRAINT IF EXISTS fk_book_age_group;
ALTER TABLE IF EXISTS public.books DROP CONSTRAINT IF EXISTS fk_book_language;

-- 8. Add new foreign key constraints with correct table names
ALTER TABLE public.books ADD CONSTRAINT fk_books_genre FOREIGN KEY (genre_id) REFERENCES public.genres(id);
ALTER TABLE public.books ADD CONSTRAINT fk_books_age_group FOREIGN KEY (age_group_id) REFERENCES public.age_groups(id);
ALTER TABLE public.books ADD CONSTRAINT fk_books_language FOREIGN KEY (language_id) REFERENCES public.languages(id);

-- 9. Rename review to reviews
ALTER TABLE IF EXISTS public.review RENAME TO reviews;

-- 10. Drop old foreign key constraints from reviews
ALTER TABLE IF EXISTS public.reviews DROP CONSTRAINT IF EXISTS fk_review_book;

-- 11. Add new foreign key constraint with correct table name
ALTER TABLE public.reviews ADD CONSTRAINT fk_reviews_book FOREIGN KEY (book_id) REFERENCES public.books(id) ON DELETE CASCADE;

-- 12. Rename order_item to order_items
ALTER TABLE IF EXISTS public.order_item RENAME TO order_items;

-- 13. Drop old foreign key constraints from order_items
ALTER TABLE IF EXISTS public.order_items DROP CONSTRAINT IF EXISTS fk_order_item_book;

-- 14. Add new foreign key constraint with correct table name
ALTER TABLE public.order_items ADD CONSTRAINT fk_order_items_book FOREIGN KEY (book_id) REFERENCES public.books(id) ON DELETE RESTRICT;

-- 15. Rename promo_code to promo_codes
ALTER TABLE IF EXISTS public.promo_code RENAME TO promo_codes;

-- 16. Update promo_codes constraints - change max_uses minimum to 1
ALTER TABLE IF EXISTS public.promo_codes DROP CONSTRAINT IF EXISTS promo_code_max_uses_check;
ALTER TABLE public.promo_codes ADD CONSTRAINT promo_codes_max_uses_check CHECK (max_uses IS NULL OR max_uses >= 1);

-- 17. Update discount_percentage constraint to allow 99.99
ALTER TABLE IF EXISTS public.promo_codes DROP CONSTRAINT IF EXISTS promo_code_discount_percentage_check;
ALTER TABLE public.promo_codes ADD CONSTRAINT promo_codes_discount_percentage_check CHECK (discount_percentage > 0.0 AND discount_percentage <= 99.99);

-- 18. Update orders foreign key to promo_codes
ALTER TABLE IF EXISTS public.orders DROP CONSTRAINT IF EXISTS fk_orders_promo_code;
ALTER TABLE public.orders ADD CONSTRAINT fk_orders_promo_code FOREIGN KEY (promo_code_id) REFERENCES public.promo_codes(id) ON DELETE SET NULL;

-- 19. Rename cart_item to cart_items
ALTER TABLE IF EXISTS public.cart_item RENAME TO cart_items;

-- 20. Drop old constraints and account_id column from cart_items
ALTER TABLE IF EXISTS public.cart_items DROP CONSTRAINT IF EXISTS fk_cart_item_account;
ALTER TABLE IF EXISTS public.cart_items DROP CONSTRAINT IF EXISTS uq_cart_item_account_book;
ALTER TABLE IF EXISTS public.cart_items DROP CONSTRAINT IF EXISTS fk_cart_item_book;

-- 21. Add order_id column to cart_items if not exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'public' 
                   AND table_name = 'cart_items' 
                   AND column_name = 'order_id') THEN
        ALTER TABLE public.cart_items ADD COLUMN order_id BIGINT NOT NULL;
    END IF;
END $$;

-- 22. Drop account_id column from cart_items
ALTER TABLE IF EXISTS public.cart_items DROP COLUMN IF EXISTS account_id;

-- 23. Add foreign key constraint for order_id and book_id
ALTER TABLE public.cart_items ADD CONSTRAINT fk_cart_items_order FOREIGN KEY (order_id) REFERENCES public.orders(id) ON DELETE CASCADE;
ALTER TABLE public.cart_items ADD CONSTRAINT fk_cart_items_book FOREIGN KEY (book_id) REFERENCES public.books(id) ON DELETE CASCADE;

-- 24. Add unique constraint for order_id and book_id
ALTER TABLE public.cart_items ADD CONSTRAINT uq_cart_items_order_book UNIQUE (order_id, book_id);

-- 25. Update payment_details - replace card_number with card_last_4_digits
ALTER TABLE IF EXISTS public.payment_details DROP COLUMN IF EXISTS card_number;
ALTER TABLE IF EXISTS public.payment_details ADD COLUMN IF NOT EXISTS card_last_4_digits VARCHAR(4);

-- 26. Update book_author junction table foreign key constraints
ALTER TABLE IF EXISTS public.book_author DROP CONSTRAINT IF EXISTS fk_book_author_book;
ALTER TABLE IF EXISTS public.book_author DROP CONSTRAINT IF EXISTS fk_book_author_author;

ALTER TABLE public.book_author ADD CONSTRAINT fk_book_author_book FOREIGN KEY (book_id) REFERENCES public.books(id) ON DELETE CASCADE;
ALTER TABLE public.book_author ADD CONSTRAINT fk_book_author_author FOREIGN KEY (author_id) REFERENCES public.authors(id) ON DELETE CASCADE;

-- 27. Drop and recreate indexes with updated table names
DROP INDEX IF EXISTS public.idx_author_name_composite;
CREATE INDEX idx_authors_name_composite ON public.authors (last_name, first_name);

DROP INDEX IF EXISTS public.idx_promo_codes_code_active;
CREATE INDEX idx_promo_codes_code_active ON public.promo_codes (code, is_active);

DROP INDEX IF EXISTS public.idx_cart_items_account;
DROP INDEX IF EXISTS public.uq_promo_code_active;
CREATE UNIQUE INDEX uq_promo_codes_code_active ON public.promo_codes (code) WHERE is_active = true;
