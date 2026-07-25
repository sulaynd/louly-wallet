-- Recipient.type was set at creation time based on whether the PHONE NUMBER was Canadian,
-- regardless of the sender's own country — meaning a Senegalese sender adding a Senegalese
-- recipient was wrongly marked INTERNATIONAL. Recompute every recipient using the correct rule:
-- NATIONAL if the recipient's currency matches the owning account's own currency.
UPDATE recipients r
SET type = CASE
    WHEN r.currency_code = (
        SELECT c.currency_code
        FROM app_users u
        JOIN countries c ON c.name = u.country
        WHERE u.username = r.owner_username
    ) THEN 'NATIONAL'
    ELSE 'INTERNATIONAL'
END
WHERE r.owner_username IS NOT NULL;
