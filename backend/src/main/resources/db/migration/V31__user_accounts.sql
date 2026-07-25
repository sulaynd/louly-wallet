CREATE TABLE user_accounts (
    id             BIGSERIAL PRIMARY KEY,
    owner_username VARCHAR(255) NOT NULL,
    type           VARCHAR(255) NOT NULL,
    currency_code  VARCHAR(10),
    balance        NUMERIC(19, 4),
    label          VARCHAR(255),
    created_at     TIMESTAMP NOT NULL
);

CREATE TABLE account_deposits (
    id                    BIGSERIAL PRIMARY KEY,
    account_id            BIGINT REFERENCES user_accounts(id),
    amount                NUMERIC(19, 4) NOT NULL,
    processed_by_username VARCHAR(255),
    created_at            TIMESTAMP NOT NULL,
    note                  VARCHAR(500)
);

-- Give every existing user a DEPOT account (going forward, registration creates one automatically).
-- Uses each user's own country's currency, not a hardcoded one — some existing accounts (e.g.
-- test accounts created during development) may not be Canadian.
INSERT INTO user_accounts (owner_username, type, currency_code, balance, label, created_at)
SELECT u.username, 'DEPOT', COALESCE(c.currency_code, 'CAD'), 0, 'Compte dépôt Louly Express', now()
FROM app_users u
LEFT JOIN countries c ON c.name = u.country
WHERE u.username NOT IN (SELECT owner_username FROM user_accounts WHERE type = 'DEPOT');

-- Demo agent account for testing in-person deposits: username "agent", password "password".
INSERT INTO app_users (username, password_hash, display_name, phone_number, country, flag_emoji, role) VALUES
('agent', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'Louly Express Agent', '+1 514 555 0111', 'Canada', '🇨🇦', 'AGENT');
