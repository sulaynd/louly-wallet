CREATE TABLE partner_ledger_entries (
    id          BIGSERIAL PRIMARY KEY,
    partner_id  BIGINT REFERENCES partners(id),
    transfer_id BIGINT REFERENCES transfers(id),
    type        VARCHAR(255) NOT NULL,
    amount      NUMERIC(19, 4) NOT NULL,
    currency    VARCHAR(10) NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    note        VARCHAR(500),
    recorded_by VARCHAR(255)
);
