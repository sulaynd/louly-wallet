-- The total commission now equals the customer-facing fee (see fee_tiers, tiered by amount) —
-- these rate columns are superseded. commission_rates now only holds the platform/reception-mode
-- split (partner_share_percent) per transaction type.
ALTER TABLE commission_rates DROP COLUMN min_rate_percent;
ALTER TABLE commission_rates DROP COLUMN max_rate_percent;
ALTER TABLE commission_rates DROP COLUMN current_rate_percent;
