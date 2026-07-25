-- Existing bank-account references created before balance simulation was added — give them the
-- same simulated starting balance new ones get, so they work in the send flow without having to
-- be re-created.
UPDATE user_accounts SET balance = 1000.00 WHERE type = 'BANCAIRE' AND balance IS NULL;
