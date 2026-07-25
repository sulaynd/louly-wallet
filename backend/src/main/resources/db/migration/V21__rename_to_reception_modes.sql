ALTER TABLE partners RENAME TO reception_modes;
ALTER TABLE recipients RENAME COLUMN partner_name TO reception_mode_name;
ALTER TABLE recipients RENAME COLUMN partner_id TO reception_mode_id;
ALTER TABLE partner_ledger_entries RENAME TO reception_mode_ledger_entries;
ALTER TABLE reception_mode_ledger_entries RENAME COLUMN partner_id TO reception_mode_id;
ALTER INDEX idx_recipients_partner_id RENAME TO idx_recipients_reception_mode_id;
