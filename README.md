# 🌌 Nebula Pulse — Full‑Stack E‑Commerce Platform

A **production‑grade, full‑stack e‑commerce ecosystem** designed for performance, scalability, and maintainability. This project features a robust backend API, a modern admin dashboard, and a responsive customer storefront.

It demonstrates real‑world engineering capabilities including high‑concurrency order handling, secure authentication, and clean frontend‑backend integration.

---

## 📖 Overview

Nebula Pulse is a full‑stack e‑commerce system built from scratch to reflect how modern web platforms are designed and maintained in production environments.

It showcases:

* Secure REST APIs for business operations
* Role‑based authentication and authorization
* High‑concurrency order and promotion handling
* Admin dashboard for business management
* Customer‑facing storefront with a complete shopping flow

This project highlights the ability to design, build, debug, and optimize real web systems.

---

## 🏗 Architecture & Modules

The repository is organized into three main monorepo modules:

| Module          | Description              | Tech Stack                                                   |
| --------------- | ------------------------ | ------------------------------------------------------------ |
| nebula-commerce | Core Backend API         | Java, Spring Boot, MyBatis Plus, Spring Security, JWT, Redis |
| nebula-admin    | Merchant/Admin Dashboard | Vue 3, TypeScript, Vite, Pinia, Element Plus                 |
| nebula-store    | Customer Storefront      | Vue 3, TypeScript, Vant UI, Mobile‑First Design              |

---

## ✨ Key Features

### 🛡 Backend (nebula-commerce)

* **High‑Performance Architecture**
  Built with Spring Boot using a modular, layered architecture suitable for microservice evolution.

* **Security First**
  Implemented Spring Security with JWT (JSON Web Token) for stateless authentication and role‑based access control (RBAC).

* **Data Persistence**
  Utilizes MyBatis Plus for efficient ORM mapping, dynamic SQL, and clean data access patterns with MySQL.

* **Caching Strategy**
  Integrated Redis to cache hot data (product details, sessions) to reduce database load and improve response times.

* **High Concurrency Handling**
  Optimized order processing logic to handle flash sales and high‑traffic scenarios safely, preventing overselling.

---

### 🖥 Admin Dashboard (nebula-admin)

* **Modern SPA**
  Developed with Vue 3 (Composition API) and TypeScript for type‑safe, maintainable frontend code.

* **State Management**
  Uses Pinia for centralized application state management across components.

* **Dynamic Routing**
  Permission‑based routing (async routes) ensuring users only see authorized pages.

* **Data Visualization**
  Integrated charts and tables for sales monitoring and basic analytics.

---

### 🛍 Storefront (nebula-store)

* **Mobile‑First Design**
  Responsive layout optimized for mobile shopping experiences using Vant UI.

* **Smooth UX**
  Fast page loads powered by Vite and optimized asset bundling.

* **Complete Shopping Flow**
  Full user journey implementation: Browsing → Cart → Checkout → Payment Mock → Order History.

---

## 🚀 Getting Started

### Prerequisites

* JDK 17+
* Node.js 18+
* MySQL 8.0
* Redis

---

### Backend Setup

```bash
cd nebula-commerce

# Configure application.yml with your MySQL and Redis credentials
mvn clean install
mvn spring-boot:run
```

---

### Admin Panel Setup

```bash
cd nebula-admin
npm install
npm run dev
```

---

### Storefront Setup

```bash
cd nebula-store
npm install
npm run dev
```

---

## 📸 Screenshots (Optional)

Add screenshots to the `docs/` folder and reference them here for better visualization:

```markdown
![Admin Dashboard](./docs/admin-dashboard.png)
![Storefront](./docs/storefront.png)
```

---

## 👨‍💻 Author

**Chengyang G.**
Full‑Stack Engineer | Bug Fix & Feature Specialist

I build complex systems and fix critical bugs in production environments.

**Specialties:** Spring Boot, Vue 3, TypeScript, System Optimization
**Experience:** 4+ years building real production systems including e‑commerce platforms and high‑concurrency applications

Open for contract work and full‑stack consulting.

---

## 📄 License

MIT License

Copyright (c) 2024 Chengyang G.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
