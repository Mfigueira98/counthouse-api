-- Tables for sign-in and the session.

-- A tenant is one company using Counthouse. Every other table points to it.
CREATE TABLE tenants (
    id               uuid    PRIMARY KEY,
    slug             text    NOT NULL UNIQUE,
    name             text    NOT NULL,
    default_currency char(3) NOT NULL CHECK (default_currency ~ '^[A-Z]{3}$'),
    -- The theme is optional.
    theme_primary    text    CHECK (theme_primary ~ '^#[0-9a-fA-F]{6}$'),
    theme_logo_url   text    CHECK (theme_logo_url LIKE 'https://%')
);

-- A user belongs to one tenant. The email is unique across all tenants,
-- so signing in needs only email and password.
CREATE TABLE users (
    id            uuid PRIMARY KEY,
    tenant_id     uuid NOT NULL REFERENCES tenants (id),
    email         text NOT NULL,
    password_hash text NOT NULL,  -- BCrypt hash, never the password
    name          text NOT NULL,
    role          text NOT NULL CHECK (role IN ('ADMIN', 'OPERATOR'))
);

-- Unique ignoring case: 'Ana@x.com' and 'ana@x.com' are the same person.
CREATE UNIQUE INDEX users_email_lower_idx ON users (lower(email));
-- PostgreSQL does not index foreign keys on its own.
CREATE INDEX users_tenant_id_idx ON users (tenant_id);

-- Only the hash of a refresh token is stored, never the token.
CREATE TABLE refresh_tokens (
    id         uuid        PRIMARY KEY,
    user_id    uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash text        NOT NULL UNIQUE,
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz  -- NULL while active; logout sets it
);

CREATE INDEX refresh_tokens_user_id_idx ON refresh_tokens (user_id);
