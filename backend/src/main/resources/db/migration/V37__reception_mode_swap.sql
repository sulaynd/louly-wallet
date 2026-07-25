UPDATE reception_modes SET active = false WHERE name = 'Wave ou Orange';
UPDATE reception_modes SET active = true, name = 'Cash pickup' WHERE name = 'BNB Cash Pickup';

-- Recipients already created with the old name keep a cached copy (reception_mode_name) that
-- doesn't auto-update when the reception_modes row is renamed — backfill it for consistency.
UPDATE recipients SET reception_mode_name = 'Cash pickup' WHERE reception_mode_name = 'BNB Cash Pickup';
