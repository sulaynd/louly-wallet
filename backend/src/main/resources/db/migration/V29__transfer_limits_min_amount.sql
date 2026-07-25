ALTER TABLE transfer_limits ADD COLUMN min_amount NUMERIC(19, 4);

-- Matches each country's current fee_tiers floor, so this migration doesn't change behavior —
-- from here on, min/max amount is governed by THIS table, not fee_tiers' tier boundaries.
UPDATE transfer_limits SET min_amount = 1 WHERE country_id = 1;      -- Canada
UPDATE transfer_limits SET min_amount = 1 WHERE country_id = 2;      -- United States
UPDATE transfer_limits SET min_amount = 1 WHERE country_id = 3;      -- France
UPDATE transfer_limits SET min_amount = 500 WHERE country_id = 4;    -- Senegal
UPDATE transfer_limits SET min_amount = 1 WHERE country_id = 5;      -- India
UPDATE transfer_limits SET min_amount = 1 WHERE country_id = 6;      -- Philippines
