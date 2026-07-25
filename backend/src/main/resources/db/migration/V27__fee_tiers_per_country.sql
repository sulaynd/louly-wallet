ALTER TABLE fee_tiers ADD COLUMN country_id BIGINT REFERENCES countries(id);

-- The original 3 tiers were given in CAD terms — assign them to Canada (id=1, per V6's seed order).
UPDATE fee_tiers SET country_id = 1;

-- Country IDs from V6's seed order: Canada=1, United States=2, France=3, Senegal=4, India=5, Philippines=6.
-- These are illustrative starting points, approximated from the CAD tiers using rates on file —
-- adjust freely via /api/admin/fee-tiers, they don't need to track exchange rates going forward.

-- United States (USD, ~0.73 per CAD)
INSERT INTO fee_tiers (country_id, min_amount, max_amount, fee_percent) VALUES
(2, 1, 70, 3),
(2, 71, 220, 4),
(2, 221, 365, 5);

-- France (EUR, ~0.66 per CAD)
INSERT INTO fee_tiers (country_id, min_amount, max_amount, fee_percent) VALUES
(3, 1, 65, 3),
(3, 66, 200, 4),
(3, 201, 330, 5);

-- Senegal (XOF, ~406 per CAD)
INSERT INTO fee_tiers (country_id, min_amount, max_amount, fee_percent) VALUES
(4, 500, 40000, 3),
(4, 40001, 120000, 4),
(4, 120001, 200000, 5);

-- India (INR, ~61.85 per CAD)
INSERT INTO fee_tiers (country_id, min_amount, max_amount, fee_percent) VALUES
(5, 1, 6000, 3),
(5, 6001, 18000, 4),
(5, 18001, 30000, 5);

-- Philippines (PHP, ~41.69 per CAD)
INSERT INTO fee_tiers (country_id, min_amount, max_amount, fee_percent) VALUES
(6, 1, 4000, 3),
(6, 4001, 12000, 4),
(6, 12001, 20000, 5);
