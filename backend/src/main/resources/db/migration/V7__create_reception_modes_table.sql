CREATE TABLE reception_modes (
    id         BIGSERIAL PRIMARY KEY,
    country_id BIGINT REFERENCES countries(id),
    name       VARCHAR(255) NOT NULL
);

-- IDs match the insert order from V6: Canada=1, United States=2, France=3, Senegal=4, India=5, Philippines=6.

-- Senegal's real reception partners
INSERT INTO reception_modes (country_id, name) VALUES
(4, 'Wave ou Orange'),
(4, 'BNB Cash Pickup'),
(4, 'Compte bancaire');

-- Generic default for the other countries until their real partner lists are known
INSERT INTO reception_modes (country_id, name) VALUES
(1, 'Compte bancaire'),
(2, 'Compte bancaire'),
(3, 'Compte bancaire'),
(5, 'Compte bancaire'),
(6, 'Compte bancaire');
