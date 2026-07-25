ALTER TABLE partners ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE partners SET active = false WHERE name = 'BNB Cash Pickup';
