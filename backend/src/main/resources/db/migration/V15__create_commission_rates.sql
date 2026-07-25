CREATE TABLE commission_rates (
    id                   BIGSERIAL PRIMARY KEY,
    type                 VARCHAR(255) NOT NULL UNIQUE,
    label                VARCHAR(255) NOT NULL,
    min_rate_percent     NUMERIC(6, 3) NOT NULL,
    max_rate_percent     NUMERIC(6, 3) NOT NULL,
    current_rate_percent NUMERIC(6, 3) NOT NULL,
    partner_share_percent NUMERIC(6, 3) NOT NULL
);

-- current_rate_percent defaults to the midpoint of each range; partner_share_percent is a
-- starting assumption (how much of the total commission the receiving/agent partner keeps) —
-- both are editable by customer service via /api/admin/commission-rates.
INSERT INTO commission_rates (type, label, min_rate_percent, max_rate_percent, current_rate_percent, partner_share_percent) VALUES
('P2P_LOCAL', 'Transfert P2P local', 0.5, 1.0, 0.75, 20),
('CASH_OUT_AGENT', 'Retrait cash (cash-out) via agent', 1.0, 1.5, 1.25, 50),
('MERCHANT_QR_PAYMENT', 'Paiement marchand (QR code)', 0.8, 1.2, 1.0, 20),
('INTERNATIONAL_INBOUND', 'Transfert international entrant (diaspora)', 1.5, 3.0, 2.25, 30),
('INTERNATIONAL_OUTBOUND_FX', 'Transfert international sortant + change de devises', 2.0, 5.0, 3.5, 30);
