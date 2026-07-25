ALTER TABLE user_accounts ADD COLUMN card_holder_name VARCHAR(255);
ALTER TABLE user_accounts ADD COLUMN card_last4 VARCHAR(4);
ALTER TABLE user_accounts ADD COLUMN card_network VARCHAR(20);
ALTER TABLE user_accounts ADD COLUMN card_expiry_month VARCHAR(2);
ALTER TABLE user_accounts ADD COLUMN card_expiry_year VARCHAR(4);

ALTER TABLE user_accounts DROP COLUMN transit_number;
ALTER TABLE user_accounts DROP COLUMN institution_number;

-- Existing BANCAIRE rows from development testing predate the card model entirely (they only
-- have the old transit/institution/account fields, now dropped) — remove them rather than leave
-- half-populated rows; real users simply re-add their card through the new form.
DELETE FROM account_movements WHERE account_id IN (SELECT id FROM user_accounts WHERE type = 'BANCAIRE');
DELETE FROM user_accounts WHERE type = 'BANCAIRE';
