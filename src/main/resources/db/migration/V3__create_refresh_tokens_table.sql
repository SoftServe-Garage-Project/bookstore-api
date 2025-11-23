-- V3__create_refresh_tokens_table.sql
-- Migration to create refresh_tokens table for JWT refresh token storage

CREATE TABLE IF NOT EXISTS public.refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_email VARCHAR(150) NOT NULL,
    token_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT false,
    revoked BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT chk_expires_after_created CHECK (expires_at > created_at)
);

-- Create indexes for performance
CREATE INDEX idx_refresh_token_user_email ON public.refresh_tokens(user_email);
CREATE INDEX idx_refresh_token_expires_at ON public.refresh_tokens(expires_at);
CREATE INDEX idx_refresh_token_token_id ON public.refresh_tokens(token_id);

-- Add comment to the table
COMMENT ON TABLE public.refresh_tokens IS 'Stores JWT refresh tokens for user authentication';
COMMENT ON COLUMN public.refresh_tokens.id IS 'Primary key, auto-generated UUID';
COMMENT ON COLUMN public.refresh_tokens.user_email IS 'Email of the user who owns this refresh token';
COMMENT ON COLUMN public.refresh_tokens.token_id IS 'Unique identifier of the JWT token';
COMMENT ON COLUMN public.refresh_tokens.created_at IS 'Timestamp when the token was created';
COMMENT ON COLUMN public.refresh_tokens.expires_at IS 'Timestamp when the token will expire';
COMMENT ON COLUMN public.refresh_tokens.used IS 'Flag indicating if the token has been used for refresh';
COMMENT ON COLUMN public.refresh_tokens.revoked IS 'Flag indicating if the token has been revoked (e.g., on logout)';

