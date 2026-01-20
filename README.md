# Nebula Commerce — Full-stack Enterprise E-commerce System

> A solo-built production-style e-commerce platform
> Backend APIs + Admin Dashboard + Store Frontend
> Designed for performance, security, and real-world business logic.

---

## 🚀 Why This Project Matters

* Complete e-commerce solution: Backend services, admin panel, and end-user store.
* Secure architecture: JWT authentication + role-based access control (RBAC).
* Performance-aware: Redis caching, optimized SQL queries, scalable module design.
* Real business features: Orders, payments, coupons, seckill (high concurrency), reviews, system notices.
* Delivery-oriented: Clear structure, reproducible setup, and documentation-first mindset.

---

## 📦 Project Modules

1. **Backend API (nebula-commerce)** — Spring Boot 3 / Redis / JWT / MyBatis Plus
2. **Admin UI (nebula-admin)** — Vue 3 / TypeScript / Element Plus
3. **Store Frontend (nebula-store)** — Vue 3 / TypeScript / Pinia

---

## 🔧 Quick Start

### Option A: Docker (recommended)

```bash
# Coming soon
# docker compose up -d
```

### Option B: Manual Development Setup

```bash
# Backend
cd nebula-commerce
mvn spring-boot:run

# Admin UI
cd nebula-admin
npm install
npm run dev

# Store UI
cd nebula-store
npm install
npm run dev
```

> Note: Requires JDK 21+, Node.js 18+, MySQL, Redis.

---

## 🔍 API Testing & Documentation

* Swagger/OpenAPI integration (to be enabled)
* Postman Collection (to be added)

These will allow interactive API testing and faster onboarding for collaborators.

---

## 🧩 Backend Architecture

Core modules:

* **auth**: JWT authentication, RBAC permission model
* **product**: Category, SKU, inventory, reviews
* **order**: Cart, order creation, payment flow
* **marketing**: Coupons, seckill (high-concurrency handling)
* **member**: User profile, address management
* **system**: Logs, notices, system configuration

---

## 🖥️ Frontend Architecture

### Admin Panel

* Dashboard
* Product & Order Management
* User & Permission Control
* Marketing Management

### Store Frontend

* Product browsing & search
* Cart & checkout
* User account center

---

## 📁 Project Structure

```
nebulapuls8888/
├── nebula-commerce   # Spring Boot backend
├── nebula-admin      # Vue3 admin panel
├── nebula-store      # Vue3 store frontend
└── docs              # Documentation & screenshots
```

---

## 🧠 What I Personally Built

* System architecture and module decomposition
* REST API design and implementation
* Authentication (JWT) & RBAC authorization
* High-concurrency marketing endpoints using Redis
* SQL optimization and backend performance tuning
* Frontend-backend integration for all business flows

---

## 📌 Roadmap

* [ ] Dockerized one-command setup
* [ ] Swagger & Postman documentation
* [ ] Database seed scripts
* [ ] CI/CD integration


