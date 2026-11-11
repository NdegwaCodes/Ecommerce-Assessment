# E-commerce Platform

A full stack online store with a Spring Boot backend and an Angular frontend. Customers browse products, manage a cart and pay with Stripe. Admins manage the catalogue and process orders.

## Features

**Shop**
- Featured products on the home page, product listing with size and category filters, product detail pages
- Cart with quantity changes and live totals
- Stripe checkout, a success page and order history for signed-in users

**Admin**
- Create and edit products and categories, and mark products as featured
- View and process orders

**Platform**
- OpenID Connect sign-in (Kinde) with role-based route guards
- Spring Security OAuth2 resource server protecting the API
- Server-side rendering for the Angular app

## Architecture

The backend follows a **hexagonal (ports and adapters), domain-driven** layout. Each bounded context keeps its business rules free of framework code:

```
apps/ecom-backend/src/main/java/ecom/
  product/   application/  domain/  infrastructure/
  order/     application/  domain/  infrastructure/   (orders, cart, Stripe sessions, users)
  shared/    authentication, error handling, JPA support
  wire/      PostgreSQL and security configuration
```

Domain objects (`Order`, `OrderPrice`, `OrderQuantity` and so on) are value objects and aggregates, built with Jilt builders. Infrastructure adapters handle REST (`primary`) and persistence and Stripe (`secondary`).

```
apps/ecom-frontend/src/app/
  home/    shop/    user/    admin/    auth/    layout/    shared/
```

`ecommerce-app-main/` holds the Nx workspace configuration (Nx project files, Tailwind, Jest and TypeScript configs, and the SSR server entry) from the original workspace.

## Tech stack

| Layer | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security (OAuth2 resource server), Bean Validation |
| Database | PostgreSQL, Liquibase migrations, Docker Compose for local Postgres |
| Payments | Stripe (stripe-java, ngx-stripe) |
| Frontend | Angular, standalone components, TanStack Query, angular-auth-oidc-client, Tailwind CSS + daisyUI, Font Awesome |
| Tooling | Nx monorepo, Maven, Jest |

## Running locally

The backend starts its own PostgreSQL container through Spring Boot's Docker Compose support (`apps/ecom-backend/compose.yaml`):

```bash
cd apps/ecom-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

It needs an `application-local.yml` with the Kinde issuer URI and the Stripe secret key. These are not committed.

The frontend reads its API URL and Stripe publishable key from `src/environments/`.
