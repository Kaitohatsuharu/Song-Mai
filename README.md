# HHSP Email SaaS

Self-hosted transactional email infrastructure with inbound reply tracking and email verification.

## Architecture

```
Mac Mini M2  →  Spring Boot API (:8080)  +  Next.js Dashboard (:3000)
VPS Hetzner  →  Postfix (outbound MTA)   +  Haraka (inbound MTA)
Synology NAS →  Email logs, attachments, DB backups
PostgreSQL   →  Primary database
Redis        →  Queue + rate limiting
```

## Quick Start (local dev)

### Prerequisites

- Java 21+
- Node.js 20+
- Docker & Docker Compose
- Maven 3.9+

### 1. Start backing services

```bash
docker compose up -d
```

This starts PostgreSQL 16 on `:5432` and Redis 7 on `:6379`.

### 2. Start Spring Boot API

```bash
cd backend
cp .env.example .env   # fill in your secrets
mvn spring-boot:run
```

API available at `http://localhost:8080`

### 3. Start Next.js Dashboard

```bash
cd frontend
npm install
npm run dev
```

Dashboard available at `http://localhost:3000`

## Project Structure

```
hhsp-email-saas/
├── backend/          # Spring Boot 3.x (Java 21)
│   └── src/main/java/dev/hhsp/email/
│       ├── controller/   # REST endpoints
│       ├── service/      # Business logic
│       ├── entity/       # JPA entities
│       ├── repository/   # Spring Data JPA
│       ├── config/       # Security, Redis, rate limiting
│       └── filter/       # API key auth filter
├── frontend/         # Next.js 14 (App Router, TypeScript)
│   └── app/
│       ├── (auth)/       # Login, Register
│       ├── dashboard/    # Protected dashboard pages
│       └── track/        # Open/click tracking
├── infra/            # VPS infrastructure configs
│   ├── postfix/      # Postfix + OpenDKIM
│   ├── haraka/       # Haraka inbound MTA + catch-all plugin
│   └── nginx/        # Nginx reverse proxy
└── docker-compose.yml
```

## API Reference

### Send Email

```bash
curl -X POST https://api.hhsp.dev/v1/email/send \
  -H "X-API-Key: sk_live_xxx" \
  -H "Content-Type: application/json" \
  -d '{
    "from": "hello@yourdomain.com",
    "to": "customer@example.com",
    "subject": "Welcome!",
    "html": "<h1>Hello!</h1>"
  }'
```

### Verify Email

```bash
curl -X POST https://api.hhsp.dev/v1/verify \
  -H "X-API-Key: sk_live_xxx" \
  -d '{"email": "someone@example.com"}'
```

### Get Message Status

```bash
curl https://api.hhsp.dev/v1/email/msg_abc123 \
  -H "X-API-Key: sk_live_xxx"
```

## Pricing

| Plan    | Price   | Emails/mo  | Verifications |
|---------|---------|------------|---------------|
| Starter | $9/mo   | 10,000     | 1,000         |
| Growth  | $29/mo  | 100,000    | 10,000        |
| Pro     | $79/mo  | 500,000    | Unlimited     |

## Environment Variables

See [`backend/src/main/resources/application.yml`](backend/src/main/resources/application.yml) for the full list of Spring Boot configuration properties.

Key variables:

| Variable              | Description                           |
|-----------------------|---------------------------------------|
| `DB_URL`              | PostgreSQL JDBC URL                   |
| `REDIS_URL`           | Redis connection URL                  |
| `INTERNAL_SECRET`     | Haraka → Spring Boot shared secret    |
| `STRIPE_SECRET_KEY`   | Stripe API key                        |
| `TRACKING_BASE_URL`   | Base URL for open/click tracking      |
| `JWT_SECRET`          | JWT signing secret (256-bit minimum)  |

## Development Roadmap

| Week   | Milestone                                            |
|--------|------------------------------------------------------|
| 1–2    | VPS setup, Postfix, DKIM/SPF/DMARC, IP warm-up      |
| 3–4    | Spring Boot API skeleton, PostgreSQL schema, API key auth |
| 5–6    | POST /v1/email/send, Redis queue, Postfix integration |
| 7–8    | Haraka inbound, /v1/inbound/webhook, thread tracking |
| 9–10   | POST /v1/verify, email verification logic            |
| 11     | Next.js dashboard (auth, email logs, domain mgmt)   |
| 12     | Stripe billing, open/click tracking, beta launch    |
