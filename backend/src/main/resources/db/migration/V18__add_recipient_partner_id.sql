ALTER TABLE recipients ADD COLUMN partner_id BIGINT REFERENCES partners(id);

-- Country-aware backfill: match by name AND by the recipient's currency == that country's
-- currency, so "Compte bancaire" (which exists once per country) resolves to the right row
-- instead of an arbitrary one.
UPDATE recipients r
SET partner_id = p.id
FROM partners p
JOIN countries c ON c.id = p.country_id
WHERE r.partner_name = p.name
  AND c.currency_code = r.currency_code
  AND r.partner_id IS NULL;

-- Fallback for country-less partners (e.g. Louly Express) that might be referenced by name only.
UPDATE recipients r
SET partner_id = p.id
FROM partners p
WHERE r.partner_id IS NULL
  AND r.partner_name = p.name
  AND p.country_id IS NULL;

CREATE INDEX idx_recipients_partner_id ON recipients(partner_id);
