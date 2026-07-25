ALTER TABLE transfers ADD COLUMN platform_commission_amount NUMERIC(19, 4);
ALTER TABLE transfers ADD COLUMN platform_commission_currency VARCHAR(10);
ALTER TABLE transfers ADD COLUMN receiving_partner_commission_amount NUMERIC(19, 4);
ALTER TABLE transfers ADD COLUMN receiving_partner_commission_currency VARCHAR(10);
