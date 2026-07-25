ALTER TABLE transfers ADD COLUMN transaction_type VARCHAR(255);
ALTER TABLE transfers ADD COLUMN commission_rate_percent NUMERIC(6, 3);
