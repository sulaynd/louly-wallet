UPDATE countries SET active = true WHERE name IN ('Canada', 'Senegal', 'France');
UPDATE countries SET active = false WHERE name NOT IN ('Canada', 'Senegal', 'France');
