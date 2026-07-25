-- The seeded XOF rate (433.50) was ~7% above the real mid-market rate and was never
-- auto-refreshed since Frankfurter (the live-rate provider) doesn't cover XOF. Correcting it to
-- the current mid-market rate (~405-407 as of July 2026). Marked MANUAL so it's clear this needs
-- periodic manual review by customer service until/unless a provider that covers XOF is wired in.
UPDATE exchange_rates
SET rate = 406.00, source = 'MANUAL', updated_at = now()
WHERE base_currency = 'CAD' AND target_currency = 'XOF';
