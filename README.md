# E-commerce Platform

[![CI](https://github.com/NdegwaCodes/Ecommerce-Assessment/actions/workflows/ci.yml/badge.svg)](https://github.com/NdegwaCodes/Ecommerce-Assessment/actions/workflows/ci.yml)

A full stack online store: a Spring Boot API and an Angular storefront in one Nx monorepo. Customers browse and filter products, build a cart and pay with Stripe. Admins manage categories, products and orders.

## Features

**Shop**
- Featured products on the home page, a catalogue filtered by category and size, product detail pages with related products
- A cart kept in the browser, with quantities and live totals
- Stripe Checkout, a confirmation page and order history for signed-in customers

**Admin**
- Create categories and products (with picture uploads), and mark products as featured
- See every order with its items, customer and status

**Platform**
- Sign-in with Kinde (OpenID Connect, authorisation code flow with refresh tokens), with `ROLE_USER` and `ROLE_ADMIN`
- The API is an OAuth2 resource server that validates Kinde access tokens; admin endpoints require `ROLE_ADMIN`
- Stripe webhooks are verified with the signing secret before an order is marked paid
- Server-side rendering and prerendering for the storefront

## Architecture

The backend follows a **hexagonal (ports and adapters), domain-driven** layout. Each bounded context keeps its business rules free of framework code:

```
apps/ecom-backend/src/main/java/com/ndegwacodes/ecom/
  product/   application/  domain/  infrastructure/   catalogue and categories
  order/     application/  domain/  infrastructure/   cart, orders, users, Stripe and Kinde adapters
  shared/    authentication, error handling, JPA support
  wire/      PostgreSQL and security configuration
```

Domain objects (`Order`, `OrderPrice`, `ProductSize` and so on) are value objects and aggregates built with Jilt builders. `infrastructure/primary` holds the REST resources and `infrastructure/secondary` the persistence, Stripe and Kinde adapters. The schema is managed by Liquibase (`src/main/resources/db/changelog`).

```
apps/ecom-frontend/src/app/
  home/  shop/  user/  admin/  auth/  layout/  shared/
```

The storefront uses standalone components, signal inputs, TanStack Query for server state and `angular-auth-oidc-client` for the Kinde session.

## Tech stack

| Layer | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 3.3, Spring Web, Spring Data JPA, Spring Security (OAuth2 resource server), Bean Validation, virtual threads |
| Database | PostgreSQL 16, Liquibase, Docker Compose for local development |
| Payments | Stripe Checkout and webhooks (stripe-java, ngx-stripe) |
| Auth | Kinde (OIDC) |
| Frontend | Angular 18, SSR, TanStack Query, Tailwind CSS + daisyUI, Font Awesome |
| Tooling | Nx 19, Maven, Jest, ESLint, GitHub Actions |

## Running locally

**Prerequisites:** Java 21, Node.js 20, Docker, a [Kinde](https://kinde.com) business and a [Stripe](https://stripe.com) account in test mode.

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
| `STRIPE_API_KEY` | your Stripe secret key (`sk_test_...`) |
| `STRIPE_WEBHOOK_SECRET` | from `stripe listen --forward-to localhost:8080/api/orders/webhook` |

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

## Checks

```bash
npm run lint
npm test                   # Jest, 25 tests
npm run build              # production build with SSR and prerendering
cd apps/ecom-backend && ./mvnw verify   # needs a PostgreSQL database, see CI
```
