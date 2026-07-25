ALTER TABLE partners RENAME COLUMN description TO description_fr;
ALTER TABLE partners ADD COLUMN description_en VARCHAR(500);
ALTER TABLE partners ADD COLUMN livrable BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE partners SET description_en = 'Send money to a digital wallet in local currency'
    WHERE name = 'Wave ou Orange';

UPDATE partners SET description_en = 'Send money to a physical location for an in-person pickup'
    WHERE name = 'BNB Cash Pickup';

UPDATE partners SET description_en = 'Send money directly to a bank account'
    WHERE name = 'Compte bancaire';

-- "Louly Express" is a delivery partner, not tied to a specific country's reception step —
-- it's the only one active for actual delivery ("livrable") for now.
INSERT INTO partners (country_id, name, livrable) VALUES
(NULL, 'Louly Express', true);
