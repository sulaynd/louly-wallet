CREATE TABLE fee_tiers (
    id          BIGSERIAL PRIMARY KEY,
    min_amount  NUMERIC(19, 4) NOT NULL,
    max_amount  NUMERIC(19, 4),
    fee_percent NUMERIC(6, 3) NOT NULL
);

-- max_amount NULL on the last row = open-ended, no upper bound yet. Customer service can add
-- more tiers above 500 later via /api/admin/fee-tiers.
INSERT INTO fee_tiers (min_amount, max_amount, fee_percent) VALUES
(1, 100, 3),
(101, 300, 4),
(301, 500, 5);
