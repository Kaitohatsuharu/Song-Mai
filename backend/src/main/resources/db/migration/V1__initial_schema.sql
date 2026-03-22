-- HHSP Email SaaS — Initial Schema
-- PostgreSQL 16+

-- Accounts (customers)
CREATE TABLE accounts (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) UNIQUE NOT NULL,
    name          VARCHAR(255),
    plan          VARCHAR(50)  DEFAULT 'starter',
    quota_monthly INTEGER      DEFAULT 10000,
    quota_used    INTEGER      DEFAULT 0,
    quota_reset   TIMESTAMP,
    stripe_id     VARCHAR(255),
    created_at    TIMESTAMP    DEFAULT NOW()
);

-- API Keys
CREATE TABLE api_keys (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id    UUID REFERENCES accounts(id) ON DELETE CASCADE,
    key_hash      VARCHAR(255) UNIQUE NOT NULL,
    key_prefix    VARCHAR(10)  NOT NULL,
    name          VARCHAR(255),
    last_used_at  TIMESTAMP,
    created_at    TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_api_keys_account_id ON api_keys(account_id);
CREATE INDEX idx_api_keys_prefix     ON api_keys(key_prefix);

-- Sending Domains
CREATE TABLE sending_domains (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id     UUID REFERENCES accounts(id) ON DELETE CASCADE,
    domain         VARCHAR(255) NOT NULL,
    dkim_selector  VARCHAR(100),
    dkim_public    TEXT,
    spf_verified   BOOLEAN DEFAULT false,
    dkim_verified  BOOLEAN DEFAULT false,
    dmarc_verified BOOLEAN DEFAULT false,
    verified_at    TIMESTAMP,
    created_at     TIMESTAMP DEFAULT NOW(),
    UNIQUE (account_id, domain)
);

-- Messages (outbound)
CREATE TABLE messages (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id    UUID REFERENCES accounts(id),
    short_id      VARCHAR(16)   UNIQUE NOT NULL,
    message_id    VARCHAR(255),
    from_address  VARCHAR(255)  NOT NULL,
    to_address    VARCHAR(255)  NOT NULL,
    subject       VARCHAR(1000),
    status        VARCHAR(50)   DEFAULT 'queued',
    provider_id   VARCHAR(255),
    sent_at       TIMESTAMP,
    delivered_at  TIMESTAMP,
    opened_at     TIMESTAMP,
    clicked_at    TIMESTAMP,
    bounced_at    TIMESTAMP,
    bounce_reason TEXT,
    created_at    TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_messages_account_id  ON messages(account_id);
CREATE INDEX idx_messages_short_id    ON messages(short_id);
CREATE INDEX idx_messages_status      ON messages(status);
CREATE INDEX idx_messages_created_at  ON messages(created_at DESC);

-- Email Events (tracking)
CREATE TABLE email_events (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id   UUID REFERENCES messages(id) ON DELETE CASCADE,
    account_id   UUID REFERENCES accounts(id),
    event_type   VARCHAR(50),
    metadata     JSONB,
    occurred_at  TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_email_events_message_id ON email_events(message_id);
CREATE INDEX idx_email_events_account_id ON email_events(account_id);

-- Threads (inbound reply tracking)
CREATE TABLE threads (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id    UUID REFERENCES accounts(id),
    original_msg  UUID REFERENCES messages(id),
    from_address  VARCHAR(255),
    subject       VARCHAR(1000),
    text_body     TEXT,
    html_body     TEXT,
    raw_headers   JSONB,
    attachments   JSONB,
    received_at   TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_threads_account_id    ON threads(account_id);
CREATE INDEX idx_threads_original_msg  ON threads(original_msg);

-- Webhook Endpoints
CREATE TABLE webhooks (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id  UUID REFERENCES accounts(id) ON DELETE CASCADE,
    url         VARCHAR(1000) NOT NULL,
    events      TEXT[],
    secret      VARCHAR(255),
    active      BOOLEAN DEFAULT true,
    created_at  TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_webhooks_account_id ON webhooks(account_id);

-- Verification Results (cache)
CREATE TABLE verifications (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id   UUID REFERENCES accounts(id),
    email        VARCHAR(255) NOT NULL,
    valid        BOOLEAN,
    disposable   BOOLEAN,
    mx_found     BOOLEAN,
    smtp_result  VARCHAR(50),
    score        INTEGER,
    cached_until TIMESTAMP,
    created_at   TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_verifications_email        ON verifications(email);
CREATE INDEX idx_verifications_cached_until ON verifications(cached_until);
