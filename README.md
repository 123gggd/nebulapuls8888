Nebula Pulse E-Commerce Platform

📖 Overview

Nebula Pulse is a production-grade, full-stack e-commerce ecosystem designed for high performance and scalability. It features a microservices-ready architecture with a robust backend, a comprehensive admin dashboard, and a responsive customer storefront.

This project demonstrates the ability to build complex systems handling high-concurrency orders, secure authentication, and real-time data management—core competencies for modern Full-Stack development.

🏗 Architecture & Modules

The repository is organized into three main monorepo modules:

Module

Description

Tech Stack

nebula-commerce

Core Backend API

Java, Spring Boot, MyBatis Plus, Spring Security, JWT

nebula-admin

Merchant/Admin Dashboard

Vue 3, TypeScript, Vite, Pinia, Element Plus

nebula-store

Customer Storefront

Vue 3, TypeScript, Vant UI, Mobile-First Design

✨ Key Features

🛡 Backend (nebula-commerce)

High-Performance Architecture: Built with Spring Boot for rapid development and microservice scalability.

Security First: Implemented Spring Security with JWT (JSON Web Token) for stateless, secure stateless authentication and Role-Based Access Control (RBAC).

Data Persistence: Utilizes MyBatis Plus for efficient ORM and dynamic SQL handling with MySQL.

Caching Strategy: Integrated Redis for caching hot data (product details, sessions) to reduce DB load and improve response times.

High Concurrency: Optimized order processing logic to handle flash sales and high-traffic scenarios safely.

🖥 Admin Dashboard (nebula-admin)

Modern SPA: Developed with Vue 3 (Composition API) and TypeScript for type-safe, maintainable frontend code.

State Management: Uses Pinia for centralized store management across components.

Dynamic Routing: Permission-based routing (Async Routes) ensuring users only see what they are authorized to access.

Data Visualization: Integrated charts and tables for real-time sales monitoring and user analytics.

🛍 Storefront (nebula-store)

Mobile-First Design: Responsive layout optimized for mobile shopping experiences using Vant UI.

Smooth UX: Fast page loads via Vite and optimized asset bundling.

Complete Flow: Full user journey implementation: Browsing -> Cart -> Checkout -> Payment Mock -> Order History.

🚀 Getting Started

Prerequisites

JDK 17+

Node.js 18+

MySQL 8.0

Redis

1. Backend Setup

cd nebula-commerce
# Configure application.yml with your MySQL/Redis credentials
mvn clean install
mvn spring-boot:run


2. Admin Panel Setup

cd nebula-admin
npm install
npm run dev


3. Storefront Setup

cd nebula-store
npm install
npm run dev


👨‍💻 Author

Chengyang G.
Full-Stack Engineer | Bug Fix & Feature Specialist

I build complex systems and fix critical bugs in production environments.

Specialties: Spring Boot, Vue 3, TypeScript, System Optimization.

Experience: 4+ years building real production systems including e-commerce platforms and high-concurrency applications.

Open for contract work and full-stack consulting.

MIT License

Copyright (c) 2024 Chengyang G.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
