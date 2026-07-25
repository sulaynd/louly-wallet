ALTER TABLE transfer_limits ADD COLUMN daily_max_amount NUMERIC(19, 4);
ALTER TABLE transfer_limits ADD COLUMN monthly_max_amount NUMERIC(19, 4);

-- Illustrative starting points (~3x per-transaction max for daily, ~10x for monthly) — adjust
-- freely via /api/admin/limits, same as the per-transaction bounds.
UPDATE transfer_limits SET daily_max_amount = 15000, monthly_max_amount = 50000 WHERE country_id = 1;        -- Canada
UPDATE transfer_limits SET daily_max_amount = 11000, monthly_max_amount = 36500 WHERE country_id = 2;         -- United States
UPDATE transfer_limits SET daily_max_amount = 10000, monthly_max_amount = 33000 WHERE country_id = 3;         -- France
UPDATE transfer_limits SET daily_max_amount = 6000000, monthly_max_amount = 20000000 WHERE country_id = 4;    -- Senegal
UPDATE transfer_limits SET daily_max_amount = 900000, monthly_max_amount = 3000000 WHERE country_id = 5;      -- India
UPDATE transfer_limits SET daily_max_amount = 600000, monthly_max_amount = 2000000 WHERE country_id = 6;      -- Philippines
