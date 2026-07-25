-- These recipients were seeded in V2, before partner_name existed (added in V3, renamed in V8).
-- Backfill a sensible partner per recipient so the ledger model actually has something to act on.
UPDATE recipients SET partner_name = 'Compte bancaire' WHERE name = 'Alexandre Roy';
UPDATE recipients SET partner_name = 'Compte bancaire' WHERE name = 'Mireille Tremblay';
UPDATE recipients SET partner_name = 'Compte bancaire' WHERE name = 'Liza Santos';
UPDATE recipients SET partner_name = 'Compte bancaire' WHERE name = 'Rohan Mehta';
UPDATE recipients SET partner_name = 'Compte bancaire' WHERE name = 'Claire Dubois';
-- Fatou Diop is the Senegal demo recipient — give her the real receiving partner, matching the
-- earlier walkthrough examples (Wave ou Orange), so the ledger model has something to demonstrate.
UPDATE recipients SET partner_name = 'Wave ou Orange' WHERE name = 'Fatou Diop';
