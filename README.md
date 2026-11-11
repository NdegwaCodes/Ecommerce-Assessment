# Mwangaza Solar

[![CI](https://github.com/NdegwaCodes/Ecommerce-Assessment/actions/workflows/ci.yml/badge.svg)](https://github.com/NdegwaCodes/Ecommerce-Assessment/actions/workflows/ci.yml)

An online store for solar products in Kenya: panels, batteries, inverters and lighting. Customers filter products by power rating, see the warranty upfront, and pay with **M-Pesa** or by card. Admins manage categories, products and orders.

A Spring Boot API and an Angular storefront in one Nx monorepo.

## What I built

The project started from the CodeCake Angular and Spring Boot e-commerce tutorial (a generic clothing shop). I rebuilt it into a Kenyan solar store and added the parts below.

- **M-Pesa checkout (Daraja STK push).** The customer enters their Safaricom number and approves the prompt on their phone. The API creates the order, sends the STK push, and settles it from Safaricom's callback, or from an STK query if the callback is late. Stock is reduced only when payment succeeds.
- **Payment safety.** An order can only leave `PENDING` once, enforced with a conditional update, so a repeated callback or webhook cannot mark it paid twice or reduce stock twice. The amount Safaricom reports must match the order total. The callback URL carries a secret token compared in constant time. I applied the same once-only rule to the existing Stripe webhook, which previously could reduce stock twice.
- **A solar product model.** Clothing size and colour are replaced by power rating (watts) and warranty (years), from the domain value objects through JPA, a Liquibase migration, the REST API and the admin form. The catalogue filter works on power bands (up to 100 W, 100 to 500 W, 500 W to 2 kW, over 2 kW).
- **Kenyan shillings throughout.** Prices display as KSh, and Stripe now charges in KES. It previously charged in euros while the shop showed dollars.
- **Brand and storefront.** New name, colours, home page and footer, and the removal of placeholder copy such as a fake "was" price on every product and an email confirmation the app never sent.
- **Restored and tested the workspace.** One buildable Nx workspace with configuration from environment variables. 30 frontend tests (Jest), backend unit tests for the Daraja helpers and order totals (JUnit), and CI that boots the API against PostgreSQL.

## How M-Pesa checkout works

```
Cart: "Pay with M-Pesa"
  -> POST /api/orders/mpesa                 checks stock, totals the cart, sends the STK push, saves a PENDING order
  -> Safaricom prompts the phone            customer enters their PIN
  -> POST /api/orders/mpesa/callback/{token}
       success and amount matches  -> PAID, receipt stored, stock reduced
       cancelled, failed, mismatch -> FAILED
  -> GET /api/orders/mpesa/{orderId}        polled by the storefront; falls back to Daraja's STK query
```

## Architecture

The backend follows a **hexagonal (ports and adapters), domain-driven** layout:

```
apps/ecom-backend/src/main/java/com/ndegwacodes/ecom/
  product/   application/  domain/  infrastructure/   catalogue, categories, power and warranty value objects
  order/     application/  domain/  infrastructure/   cart, orders, users; Stripe, M-Pesa and Kinde adapters
  shared/    authentication, error handling, JPA support
  wire/      PostgreSQL and security configuration
```

`infrastructure/primary` holds the REST resources and `infrastructure/secondary` the persistence and payment adapters. The schema is managed by Liquibase (`src/main/resources/db/changelog`).

```
apps/ecom-frontend/src/app/
  home/  shop/  user/  admin/  auth/  layout/  shared/
```

The storefront uses standalone components, signals, TanStack Query for server state and `angular-auth-oidc-client` for the Kinde session.

## Tech stack

| Layer | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 3.3, Spring Web, Spring Data JPA, Spring Security (OAuth2 resource server), RestClient, virtual threads |
| Database | PostgreSQL 16, Liquibase, Docker Compose for local development |
| Payments | M-Pesa Daraja API (STK push, STK query, result callback), Stripe Checkout and webhooks |
| Auth | Kinde (OIDC) |
| Frontend | Angular 18, SSR, TanStack Query, Tailwind CSS + daisyUI, Font Awesome |
| Tooling | Nx 19, Maven, Jest, JUnit 5, ESLint, GitHub Actions |

## Running locally

**Prerequisites:** Java 21, Node.js 20, Docker, a [Kinde](https://kinde.com) business, a [Daraja](https://developer.safaricom.co.ke) app (the sandbox is free) and, for card payments, a [Stripe](https://stripe.com) account in test mode.

### 1. Kinde

Create, in your Kinde business:
- a **front-end app** (single-page application) with `http://localhost:4200` as the callback and logout URL
- an **API** with the audience `http://localhost:8080/api`, enabled for that app
- a **machine-to-machine app** with access to the Kinde Management API (the backend uses it to read user profiles)
- the roles `ROLE_USER` and `ROLE_ADMIN`, added to the access token as the `roles` claim

### 2. Backend

Set these environment variables, then start the API. Spring Boot starts PostgreSQL from `apps/ecom-backend/compose.yaml` automatically.

| Variable | Value |
| --- | --- |
| `KINDE_ISSUER_URI` | `https://YOUR_SUBDOMAIN.kinde.com` |
| `KINDE_CLIENT_ID`, `KINDE_CLIENT_SECRET` | the machine-to-machine app's credentials |
| `MPESA_CONSUMER_KEY`, `MPESA_CONSUMER_SECRET`, `MPESA_PASSKEY` | from your Daraja app |
| `MPESA_SHORTCODE` | `174379` in the sandbox |
| `MPESA_CALLBACK_BASE_URL` | a public HTTPS address for the API, such as an ngrok tunnel |
| `MPESA_CALLBACK_TOKEN` | a long random string |
| `MPESA_ENV` | `sandbox` (default) or `production` |
| `STRIPE_API_KEY`, `STRIPE_WEBHOOK_SECRET` | your Stripe test keys, the secret from `stripe listen --forward-to localhost:8080/api/orders/webhook` |

M-Pesa is optional: without its variables the store runs and the M-Pesa button explains that it is unavailable.

```bash
npm install
npm run start:backend      # http://localhost:8080
```

Without Docker, point `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` at any PostgreSQL database.

### 3. Storefront

Put your Kinde domain, front-end app client ID and Stripe publishable key in `apps/ecom-frontend/src/environments/environment.ts`, then:

```bash
npm start                  # http://localhost:4200
```

The store's name and tagline live in `apps/ecom-frontend/src/app/shared/brand.ts`.

## Checks

```bash
npm run lint
npm test                   # Jest
npm run build              # production build with SSR and prerendering
cd apps/ecom-backend && ./mvnw verify   # needs a PostgreSQL database, see CI
```
