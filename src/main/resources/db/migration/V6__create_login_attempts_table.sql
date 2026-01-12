-- V6__create_login_attempts_table.sql
-- Table for tracking failed login attempts and implementing rate limiting

CREATE TABLE IF NOT EXISTS login_attempts (
    id BIGSERIAL PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL UNIQUE,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_attempt_time TIMESTAMP NOT NULL,
    blocked_until TIMESTAMP
);

-- Index for faster lookups by identifier
CREATE INDEX IF NOT EXISTS idx_login_attempts_identifier ON login_attempts(identifier);

-- Index for cleanup queries (blocked_until and last_attempt_time)
CREATE INDEX IF NOT EXISTS idx_login_attempts_blocked_until ON login_attempts(blocked_until);
CREATE INDEX IF NOT EXISTS idx_login_attempts_last_attempt_time ON login_attempts(last_attempt_time);

COMMENT ON TABLE login_attempts IS 'Stores failed login attempts for rate limiting and brute-force protection';
COMMENT ON COLUMN login_attempts.identifier IS 'IP address or other identifier of the login attempt source';
COMMENT ON COLUMN login_attempts.attempts IS 'Number of failed login attempts';
COMMENT ON COLUMN login_attempts.last_attempt_time IS 'Timestamp of the last failed attempt';
COMMENT ON COLUMN login_attempts.blocked_until IS 'If set, the identifier is blocked until this time';

