# Meridian — money transfer app (full stack)

Angular 17 frontend + Spring Boot 3 REST API + PostgreSQL, with JWT auth and Flyway-managed migrations.

## Option A — Docker (everything in one command)

```
docker compose up --build
```

This builds and starts all three services:
- **Postgres** — `localhost:5433`
- **Backend API** — `http://localhost:8080`
- **Frontend** — `http://localhost:4200`

Flyway runs the migrations and seeds demo data automatically on first startup. Open
`http://localhost:4200` and log in with the demo account below.

To reset everything (fresh database): `docker compose down -v && docker compose up --build`.

## Option B — Run locally without Docker

### 1. Start Postgres

```
cd backend
docker compose up -d
```

(No Docker? Install Postgres locally on port 5433 with database/user/password `meridian`, or edit
`backend/src/main/resources/application.yml`.)

### 2. Start the backend

```
cd backend
mvn spring-boot:run
```

Flyway creates the schema and seeds demo data on first run (`db/migration/`). The API listens on
`http://localhost:8080`.

### 3. Start the frontend

```
cd frontend
npm install
npm start
```

Open `http://localhost:4200`.

## Logging in

You'll land on a login/create-account screen. **Demo account:** `demo` / `password` (seeded
automatically). **Customer-service account:** `support` / `password` (can manage rates and the
transfer cap — see below). Or create your own account — each account gets its own private
transfer history and recipient directory; nothing is shared between accounts.

## Authentication

Login is JWT-based: `POST /api/auth/login` with `{ "username", "password" }` returns a token,
which the frontend then sends as `Authorization: Bearer <token>` on every subsequent request
(handled automatically by `auth.interceptor.ts`). Tokens expire after 24h (`jwt.expiration-ms`
in `application.yml`) and are only held in memory on the frontend — refreshing the page logs
you out, same tradeoff as before.

Key endpoints:
- `POST /api/auth/register` — body `{ "username", "password", "displayName", "phoneNumber", "country" }` (public)
- `POST /api/auth/login` — body `{ "username", "password" }` → `{ "token", "username", "displayName", ... }` (public)
- `GET  /api/auth/me` — confirms the current token is valid, returns fresh profile info (requires a token)
- `GET  /api/recipients?type=NATIONAL|INTERNATIONAL` (requires a token)
- `GET  /api/rates?from=CAD&to=PHP` (requires a token)
- `POST /api/transfers` — body `{ "recipientId", "amount", "amountReceived", "rate", "fee" }` (requires a token; transfer is owned by the authenticated user)
- `GET  /api/transfers` — full transfer history for the authenticated user
- `GET  /api/transfers/{id}` / `GET /api/transfers/latest` (only the authenticated user's own transfers)

**Rate & limit management (requires ROLE_ADMIN — use `support` / `password`):**
- `GET  /api/admin/rates` — list every currency's current rate, source (`SEED` / `LIVE_PROVIDER` / `MANUAL`), and last-updated time
- `PUT  /api/admin/rates/{currency}` — body `{ "rate": 435.00 }`, manually corrects one currency
- `POST /api/admin/rates/refresh` — forces the scheduled provider refresh to run immediately
- `POST /api/admin/rates/upload` — multipart file upload, one `CURRENCY,RATE` pair per line
- `GET  /api/admin/limits` / `PUT /api/admin/limits` — view/change the per-transfer safety cap
- `GET  /api/admin/countries` — list every country (active or not)
- `PUT  /api/admin/countries/{id}` — body `{ "active": false }`, activates/deactivates a country
- `GET  /api/admin/partners` / `PUT /api/admin/partners/{id}` — manage `active`/`livrable`/commission per partner
- `GET  /api/admin/accounting/summary` — revenue, transaction count, partner commission expense
- `GET  /api/admin/accounting/balances` — current amount owed to each partner
- `GET  /api/admin/accounting/ledger` — full ledger history (auto entries + manual settlements)
- `POST /api/admin/accounting/settlements` — record an actual payment made to a partner
- `GET  /api/admin/commission-rates` — the 5 commission buckets (type, range, current rate, partner share)
- `PUT  /api/admin/commission-rates/{id}` — body `{ "currentRatePercent": 0.9, "partnerSharePercent": 25 }`, validated against the bucket's min/max

Public (no token needed): `GET /api/countries` — the active-only country list used by the
registration and add-recipient forms.

All `/api/admin/**` calls need a Bearer token from the `support` account — get one first:
```
TOKEN=$(curl -s -X POST -H "Content-Type: application/json" \
  -d '{"username":"support","password":"password"}' \
  http://localhost:8080/api/auth/login | jq -r .token)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/admin/rates
```

## How exchange rates work

Rates live in the `exchange_rates` table (base currency is always CAD) — `ExchangeRateService.rateFor()`
reads straight from there instead of calling the live provider on every request. Two things keep it current:

- **Scheduled refresh** (`@Scheduled` in `ExchangeRateService.refreshFromProvider()`) runs 15 seconds after
  startup and then every hour, pulling fresh rates from **Frankfurter** for any currency it covers
  (PHP/INR/EUR/...), and from a **secondary provider, [ExchangeRate-API](https://www.exchangerate-api.com/)**,
  for currencies Frankfurter doesn't cover — currently just XOF. The secondary provider only runs if
  `EXCHANGERATE_API_KEY` is set (see below); without it, those currencies simply keep whatever value is on file.
- **Customer service** can override any rate at any time — a single edit or a bulk CSV upload — through
  the `support` account. Manually set rates are marked `MANUAL` in the table; the next scheduled refresh
  still overwrites any currency a provider covers back to the live rate.

**Enabling the XOF auto-refresh:** sign up for a free key at
[exchangerate-api.com](https://www.exchangerate-api.com/) (free tier covers this easily), then set it as an
environment variable before starting:
```
export EXCHANGERATE_API_KEY=your-key-here
docker compose up --build
```
Without a key, XOF stays exactly where customer service last set it (or the seed value) — nothing breaks,
it just doesn't auto-refresh.

## Commission model (business logic)

Commission is a **percentage of the amount sent, by transaction type** — not a flat fee anymore.
The grid (`commission_rates` table, seeded in `V15__create_commission_rates.sql`):

| Type | Range | Seeded rate | Partner share |
|---|---|---|---|
| Transfert P2P local | 0.5–1% | 0.75% | 20% |
| Retrait cash (cash-out) via agent | 1–1.5% | 1.25% | 50% |
| Paiement marchand (QR code) | 0.8–1.2% | 1.0% | 20% |
| Transfert international entrant (diaspora) | 1.5–3% | 2.25% | 30% |
| Transfert international sortant + change de devises | 2–5% | 3.5% | 30% |

Each transfer picks a bucket (a simplified heuristic for now — see `TransferService.detectTransactionType()`:
a "Cash Pickup" partner name → cash-out via agent; otherwise national → P2P local, international → outbound FX;
the QR-payment and inbound-diaspora buckets exist for flows not built yet), computes the total commission as
`amountSent × currentRatePercent`, then splits it: `partnerSharePercent` of that total goes to the receiving
partner (e.g. Wave ou Orange) if there is one, converted to their currency at the transfer's exchange rate;
the rest is Louly Express's platform revenue (always CAD). Both `currentRatePercent` (must stay within the
type's min/max) and `partnerSharePercent` are editable any time by customer service:

```
curl -X PUT -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"currentRatePercent": 0.9, "partnerSharePercent": 25}' http://localhost:8080/api/admin/commission-rates/{id}
```

Every transfer snapshots which bucket and rate applied (`transactionType`, `commissionRatePercent`,
`platformCommissionAmount`/`Currency`, `receivingPartnerCommissionAmount`/`Currency` on `Transfer`) — same
principle as the frozen rate/fee: changing a rate later never rewrites what a past transaction actually earned.

The `partners.commission_amount`/`commission_currency` columns from the earlier flat-fee model still exist
in the schema but are no longer read by `TransferService` — safe to ignore or repurpose later.

## How countries work

Supported countries (name, flag, currency, calling code, active/inactive) live in a real
`countries` table instead of hardcoded lists — seeded in `V6__create_countries_table.sql` with
the 6 currently supported: Canada, United States, France, Senegal, India, Philippines, all active.
`PhoneCountryResolver` (used when adding a recipient) and the registration form both only consider
**active** countries. Customer service can activate/deactivate any country at any time via
`/api/admin/countries` — no redeploy needed, and it immediately affects both what shows up in the
country dropdowns and what phone numbers get auto-detected.

## Database migrations

Schema is managed by [Flyway](https://flywaydb.org/) (`backend/src/main/resources/db/migration/`), not
`ddl-auto` (which is set to `validate` — Hibernate checks the entities match the schema but never changes
it). To make a schema change: add a new `V{n}__description.sql` file (never edit a migration that's already
run anywhere) and Flyway applies it automatically on next startup. Seed/demo data lives in `V2__seed_demo_data.sql`
— it only runs once, ever, so demo data no longer resets on every restart. To get a clean dataset again, reset
the database itself (`docker compose down -v && docker compose up -d` from `backend/`, or the root compose file).

## Notes / next steps for production

- **JWT secret**: `application.yml` defaults to a placeholder dev secret. Set a real one via the
  `JWT_SECRET` environment variable before deploying anywhere real (already wired up in the root
  `docker-compose.yml` — set it in your shell or a `.env` file).
- Recipients are now scoped per user (`ownerUsername` on `Recipient`), same as transfers — each
  account has its own private address book. The demo account's 6 seeded recipients were assigned
  to it in `V5__add_recipient_owner.sql`; new accounts start with an empty directory.
- CORS is opened only for `http://localhost:4200` and `:4201`; update `SecurityConfig.corsConfigurationSource()` for other environments/domains.
- Backend UI strings coming from stored data (recipient names, delivery partners) aren't translated — only static labels and transfer-timeline event types are, via `I18nService`.
- No refresh-token flow yet — when a JWT expires the person just has to log in again. Fine for a demo; add
  a refresh-token endpoint before shipping anything real.
