-- Louly Express was global (country_id NULL) — showing for every country. Now Senegal-only.
UPDATE reception_modes
SET country_id = (SELECT id FROM countries WHERE name = 'Senegal'), active = true
WHERE name = 'Louly Express';

-- Cash pickup (Senegal) — confirm active (already set in V37, harmless if repeated).
UPDATE reception_modes SET active = true
WHERE name = 'Cash pickup' AND country_id = (SELECT id FROM countries WHERE name = 'Senegal');

-- Compte bancaire — active only for Canada and France, deactivated for Senegal.
UPDATE reception_modes SET active = true
WHERE name = 'Compte bancaire' AND country_id IN (SELECT id FROM countries WHERE name IN ('Canada', 'France'));

UPDATE reception_modes SET active = false
WHERE name = 'Compte bancaire' AND country_id = (SELECT id FROM countries WHERE name = 'Senegal');
