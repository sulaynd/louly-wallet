CREATE TABLE countries (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL UNIQUE,
    flag_emoji    VARCHAR(255),
    currency_code VARCHAR(255),
    calling_code  VARCHAR(255),
    active        BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO countries (name, flag_emoji, currency_code, calling_code, active) VALUES
('Canada', '🇨🇦', 'CAD', '+1', true),
('United States', '🇺🇸', 'USD', '+1', true),
('France', '🇫🇷', 'EUR', '+33', true),
('Senegal', '🇸🇳', 'XOF', '+221', true),
('India', '🇮🇳', 'INR', '+91', true),
('Philippines', '🇵🇭', 'PHP', '+63', true);
