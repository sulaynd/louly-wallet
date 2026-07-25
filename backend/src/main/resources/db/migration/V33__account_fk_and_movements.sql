-- 1. Real FK for the account owner, replacing owner_username.
ALTER TABLE user_accounts ADD COLUMN owner_user_id BIGINT REFERENCES app_users(id);
UPDATE user_accounts ua SET owner_user_id = u.id
    FROM app_users u WHERE u.username = ua.owner_username;
ALTER TABLE user_accounts ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE user_accounts DROP COLUMN owner_username;

-- 2. Account number + bank-specific structured fields.
ALTER TABLE user_accounts ADD COLUMN account_number VARCHAR(50);
ALTER TABLE user_accounts ADD COLUMN transit_number VARCHAR(10);
ALTER TABLE user_accounts ADD COLUMN institution_number VARCHAR(10);

-- Generate an account number for every existing DEPOT account.
UPDATE user_accounts
SET account_number = 'LE' || LPAD(id::text, 8, '0')
WHERE type = 'DEPOT' AND account_number IS NULL;

-- 3. Unified movement history, replacing the deposit-only ledger.
CREATE TABLE account_movements (
    id                   BIGSERIAL PRIMARY KEY,
    account_id           BIGINT REFERENCES user_accounts(id),
    type                 VARCHAR(255) NOT NULL,
    amount               NUMERIC(19, 4) NOT NULL,
    balance_after        NUMERIC(19, 4),
    related_transfer_id  BIGINT REFERENCES transfers(id),
    processed_by_user_id BIGINT REFERENCES app_users(id),
    created_at           TIMESTAMP NOT NULL,
    note                 VARCHAR(500)
);

-- Migrate whatever's already in account_deposits (e.g. test deposits made during development).
INSERT INTO account_movements (account_id, type, amount, balance_after, processed_by_user_id, created_at, note)
SELECT d.account_id, 'DEPOSIT', d.amount, ua.balance, u.id, d.created_at, d.note
FROM account_deposits d
JOIN user_accounts ua ON ua.id = d.account_id
LEFT JOIN app_users u ON u.username = d.processed_by_username;

DROP TABLE account_deposits;
