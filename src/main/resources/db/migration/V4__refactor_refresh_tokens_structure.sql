-- V4__refactor_refresh_tokens_structure.sql
-- Refactor refresh_tokens table: remove id column, make token_id the primary key

ALTER TABLE public.refresh_tokens DROP CONSTRAINT IF EXISTS refresh_tokens_pkey;
ALTER TABLE public.refresh_tokens DROP CONSTRAINT IF EXISTS refresh_tokens_token_id_key;
DROP INDEX IF EXISTS public.idx_refresh_token_token_id;
ALTER TABLE public.refresh_tokens DROP COLUMN IF EXISTS id;
ALTER TABLE public.refresh_tokens ADD PRIMARY KEY (token_id);
