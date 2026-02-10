-- OAuth2 JWK 持久化表（MySQL / PostgreSQL / SQLite 通用方言）
CREATE TABLE IF NOT EXISTS oauth2_jwk (
    kid VARCHAR(128) PRIMARY KEY,
    status VARCHAR(16) NOT NULL,
    public_jwk TEXT NOT NULL,
    private_jwk TEXT NULL,
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

