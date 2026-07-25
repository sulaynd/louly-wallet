TRUNCATE TABLE transfer_events, transfers, recipients, app_users, exchange_rates RESTART IDENTITY CASCADE;

-- Demo account: username "demo", password "password" (bcrypt hash below matches that password).
INSERT INTO app_users (username, password_hash, display_name, phone_number, country, flag_emoji, role) VALUES
('demo', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'Demo user', '+1 514 555 0134', 'Canada', '🇨🇦', 'USER');

-- Customer-service account: username "support", password "password" — can manage exchange_rates.
INSERT INTO app_users (username, password_hash, display_name, phone_number, country, flag_emoji, role) VALUES
('support', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'Customer Service', '+1 514 555 0100', 'Canada', '🇨🇦', 'ADMIN');

INSERT INTO recipients (name, type, detail, flag_emoji, currency_code, phone_number) VALUES
('Alexandre Roy', 'NATIONAL', 'RBC •••• 4471', '🇨🇦', 'CAD', '+1 514 555 0142'),
('Mireille Tremblay', 'NATIONAL', 'Desjardins •••• 0092', '🇨🇦', 'CAD', '+1 438 555 0199'),
('Liza Santos', 'INTERNATIONAL', 'BDO Bank, Manila', '🇵🇭', 'PHP', '+63 917 555 0142'),
('Rohan Mehta', 'INTERNATIONAL', 'HDFC Bank, Mumbai', '🇮🇳', 'INR', '+91 98765 43210'),
('Claire Dubois', 'INTERNATIONAL', 'BNP Paribas, Lyon', '🇫🇷', 'EUR', '+33 6 12 34 56 78'),
('Fatou Diop', 'INTERNATIONAL', 'Orange Money, Dakar', '🇸🇳', 'XOF', '+221 78 149 90 51');

-- Starting rates (base = CAD). The scheduled job refreshes PHP/INR/EUR from the live provider;
-- XOF isn't covered by it, so it stays at this seed value until support updates it.
INSERT INTO exchange_rates (base_currency, target_currency, rate, source, updated_at) VALUES
('CAD', 'PHP', 41.69, 'SEED', now()),
('CAD', 'INR', 61.85, 'SEED', now()),
('CAD', 'EUR', 0.66, 'SEED', now()),
('CAD', 'XOF', 433.50, 'SEED', now());
