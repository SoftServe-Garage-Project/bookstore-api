-- V5__create_deactivated_tokens_table.sql
-- Migration to create deactivated_token table for storing invalidated JWT access tokens

CREATE TABLE IF NOT EXISTS public.deactivated_token (
    id UUID PRIMARY KEY,
    deactivated_at TIMESTAMP NOT NULL,
    keep_until TIMESTAMP NOT NULL,
    CONSTRAINT chk_deactivated_keep_until_after_deactivation CHECK (keep_until > deactivated_at)
);

CREATE INDEX idx_deactivated_token_id ON public.deactivated_token(id);
CREATE INDEX idx_deactivated_token_keep_until ON public.deactivated_token(keep_until);

COMMENT ON TABLE public.deactivated_token IS 'Stores deactivated JWT access tokens (by token ID) until their natural expiration';
COMMENT ON COLUMN public.deactivated_token.id IS 'Token ID (jti claim from JWT), serves as primary key';
COMMENT ON COLUMN public.deactivated_token.deactivated_at IS 'Timestamp when the token was deactivated';
COMMENT ON COLUMN public.deactivated_token.keep_until IS 'Timestamp until which this record should be kept (original token expiration time)';

