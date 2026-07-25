ALTER TABLE partners ADD COLUMN commission_amount NUMERIC(19, 4);
ALTER TABLE partners ADD COLUMN commission_currency VARCHAR(10);

-- Louly Express is the platform/delivery partner — earns 0.99 CAD on every transaction.
UPDATE partners SET commission_amount = 0.99, commission_currency = 'CAD'
    WHERE name = 'Louly Express';

-- Wave ou Orange is the receiving/payout partner in Senegal — earns 0.30 XOF per payout.
UPDATE partners SET commission_amount = 0.30, commission_currency = 'XOF'
    WHERE name = 'Wave ou Orange';
