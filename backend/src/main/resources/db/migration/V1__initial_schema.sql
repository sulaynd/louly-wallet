-- Baseline schema, matching the JPA entities as of the switch from ddl-auto:update to Flyway.
-- Any future schema change gets its own V2__..., V3__... file — never edit this one after
-- it has run anywhere.

CREATE TABLE app_users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(255),
    phone_number  VARCHAR(255),
    country       VARCHAR(255),
    flag_emoji    VARCHAR(255),
    role          VARCHAR(255) NOT NULL DEFAULT 'USER'
);

CREATE TABLE recipients (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255),
    type          VARCHAR(255),
    detail        VARCHAR(255),
    flag_emoji    VARCHAR(255),
    currency_code VARCHAR(255),
    phone_number  VARCHAR(255)
);

CREATE TABLE transfers (
    id               BIGSERIAL PRIMARY KEY,
    recipient_id     BIGINT REFERENCES recipients(id),
    mode             VARCHAR(255),
    amount_sent      NUMERIC(19, 2),
    amount_received  NUMERIC(19, 2),
    source_currency  VARCHAR(255),
    target_currency  VARCHAR(255),
    exchange_rate    NUMERIC(19, 6),
    fee              NUMERIC(19, 2),
    total_charged    NUMERIC(19, 2),
    status           VARCHAR(255),
    created_at       TIMESTAMP,
    owner_username   VARCHAR(255)
);

CREATE TABLE transfer_events (
    id           BIGSERIAL PRIMARY KEY,
    transfer_id  BIGINT REFERENCES transfers(id),
    type         VARCHAR(255),
    title        VARCHAR(255),
    subtitle     VARCHAR(255),
    occurred_at  TIMESTAMP,
    pending      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE exchange_rates (
    id              BIGSERIAL PRIMARY KEY,
    base_currency   VARCHAR(255) NOT NULL,
    target_currency VARCHAR(255) NOT NULL,
    rate            NUMERIC(19, 6) NOT NULL,
    source          VARCHAR(255),
    updated_at      TIMESTAMP,
    UNIQUE (base_currency, target_currency)
);

CREATE TABLE transfer_limits (
    id          BIGSERIAL PRIMARY KEY,
    max_amount  NUMERIC(19, 2),
    updated_at  TIMESTAMP,
    updated_by  VARCHAR(255)
);
