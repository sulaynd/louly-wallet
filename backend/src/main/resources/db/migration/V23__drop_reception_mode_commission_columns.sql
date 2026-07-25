-- Superseded by the percentage-by-transaction-type model (commission_rates table). These columns
-- were never read by TransferService after that model was introduced.
ALTER TABLE reception_modes DROP COLUMN commission_amount;
ALTER TABLE reception_modes DROP COLUMN commission_currency;
