ALTER TABLE reception_modes RENAME TO partners;
ALTER TABLE recipients RENAME COLUMN reception_mode TO partner_name;
