ALTER TABLE recipients
    ADD COLUMN owner_username VARCHAR(255);

-- The 6 recipients seeded in V2 had no owner — assign them to the demo account so it still
-- has a personal address book to test with. Any account created after this migration starts
-- with an empty directory of its own.
UPDATE recipients SET owner_username = 'demo' WHERE owner_username IS NULL;
