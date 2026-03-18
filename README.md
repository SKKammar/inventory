# Inventory & Order Management System

A production-ready REST API backend for managing inventory and orders, built with Java 17, Spring Boot 3.2, MySQL 8, and JWT authentication.

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen) ![MySQL](https://img.shields.io/badge/MySQL-8.0-blue) ![JWT](https://img.shields.io/badge/JWT-0.12.3-red) ![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203.0-green)

## Features

- **User Management** — Registration, login, role-based access control (ADMIN / CUSTOMER / WAREHOUSE_STAFF)
- **Product Management** — Full CRUD, category filtering, SKU lookup
- **Order Management** — Order lifecycle (PENDING → CONFIRMED → SHIPPED → DELIVERED), payment tracking
- **Inventory Control** — Real-time stock updates, movement history, low-stock alerts
- **JWT Authentication** — Stateless auth with access + refresh tokens
- **Swagger UI** — Interactive API docs at `/swagger-ui.html`
- **Global Exception Handling** — Consistent JSON error responses
- **Data Seeding** — Roles and admin user auto-created on startup

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.0 |
| Database | MySQL 8.0 |
| ORM | Hibernate / Spring Data JPA |
| Security | Spring Security 6 + JWT (JJWT 0.12.3) |
| Documentation | Springdoc OpenAPI 2.1.0 |
| Build | Maven 3.9+ |
| Utilities | Lombok, ModelMapper |

## Prerequisites

- Java 17+
- MySQL 8.0+
- Maven 3.9+

## Quick Start

### 1. Create MySQL Database
```sql
CREATE DATABASE inventory_db;
```

### 2. Configure Database
Edit `src/main/resources/application.yaml`:
```yaml
spring:
  datasource:
    username: YOUR_MYSQL_USERNAME
    password: YOUR_MYSQL_PASSWORD
```

### 3. Run the Application
```bash
mvn spring-boot:run
```

### 4. Access Swagger UI
Open: http://localhost:8080/swagger-ui.html

### 5. Default Admin Credentials
```
Username: admin
Password: Admin@123
```

## API Endpoints

### Authentication (`/api/auth`)
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/login` | Login and get JWT | Public |
| POST | `/api/auth/signup` | Register new user | Public |
| POST | `/api/auth/refresh-token` | Refresh access token | Public |
| POST | `/api/auth/logout` | Invalidate refresh token | Public |

### Products (`/api/products`)
| Method | Endpoint | Description | Role |
|---|---|---|---|
| GET | `/api/products` | List all active products | Authenticated |
| GET | `/api/products/{id}` | Get product by ID | Authenticated |
| GET | `/api/products/sku/{sku}` | Get product by SKU | Authenticated |
| GET | `/api/products/category/{cat}` | Filter by category | Authenticated |
| GET | `/api/products/stock/low` | Low stock alerts | ADMIN / WAREHOUSE |
| POST | `/api/products` | Create product | ADMIN |
| PUT | `/api/products/{id}` | Update product | ADMIN |
| DELETE | `/api/products/{id}` | Soft-delete product | ADMIN |

### Orders (`/api/orders`)
| Method | Endpoint | Description | Role |
|---|---|---|---|
| POST | `/api/orders` | Create order | Authenticated |
| GET | `/api/orders` | List all orders | ADMIN |
| GET | `/api/orders/{id}` | Get order by ID | Authenticated |
| GET | `/api/orders/user/{userId}` | Get user's orders | Authenticated |
| GET | `/api/orders/status/{status}` | Filter by status | ADMIN |
| PUT | `/api/orders/{id}/status` | Update order status | ADMIN / WAREHOUSE |
| PUT | `/api/orders/{id}/payment-status` | Update payment | ADMIN / WAREHOUSE |
| DELETE | `/api/orders/{id}` | Cancel order | Authenticated |
| GET | `/api/orders/stats/all` | Order statistics | ADMIN |

### Inventory (`/api/inventory`)
| Method | Endpoint | Description | Role |
|---|---|---|---|
| GET | `/api/inventory/stock/{productId}` | Get stock level | ADMIN / WAREHOUSE |
| GET | `/api/inventory/history/{productId}` | Stock history | ADMIN / WAREHOUSE |
| GET | `/api/inventory/low-stock` | Low stock alerts | ADMIN / WAREHOUSE |
| POST | `/api/inventory/adjustment` | Manual stock adjustment | ADMIN |

## Sample API Calls

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'
```

### Create Product (with token)
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MacBook Pro",
    "sku": "MBP-001",
    "category": "Electronics",
    "price": 1299.99,
    "currentStock": 25,
    "minStockLevel": 5,
    "unit": "PCS"
  }'
```

### Create Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "shippingAddress": "123 Main St",
    "items": [{"productId": 1, "quantity": 2}]
  }'
```

## Project Structure

```
src/main/java/com/example/inventory/
├── Application.java
├── config/          # SecurityConfig, OpenApiConfig, GlobalExceptionHandler
├── controller/      # REST controllers (Auth, Product, Order, Inventory, User)
├── dto/             # Request/Response DTOs
├── entity/          # JPA entities (User, Product, Order, OrderItem, Inventory, Role)
├── exception/       # Custom exceptions
├── init/            # DataInitializer (seeds roles + admin)
├── repository/      # Spring Data JPA repositories
├── security/        # JWT filter, token provider, UserDetailsService
└── service/         # Business logic (Auth, Product, Order, Inventory, User)
```
