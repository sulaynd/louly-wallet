ALTER TABLE transfer_limits ADD COLUMN country_id BIGINT REFERENCES countries(id);

-- The original single row (5000.00) was in CAD terms — assign it to Canada (id=1, per V6's seed order).
UPDATE transfer_limits SET country_id = 1;

-- Country IDs from V6's seed order: Canada=1, United States=2, France=3, Senegal=4, India=5, Philippines=6.
-- Approximated from the 5000 CAD default using rates on file — illustrative starting points,
-- adjust freely via /api/admin/limits, they don't need to track exchange rates going forward.
INSERT INTO transfer_limits (country_id, max_amount, updated_at, updated_by) VALUES
(2, 3650.00, now(), 'system-default'),      -- United States (USD)
(3, 3300.00, now(), 'system-default'),      -- France (EUR)
(4, 2000000.00, now(), 'system-default'),   -- Senegal (XOF)
(5, 300000.00, now(), 'system-default'),    -- India (INR)
(6, 200000.00, now(), 'system-default');    -- Philippines (PHP)
