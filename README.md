# Spring Boot backend

## Project overview

REST API for the food ordering application. It is built with **Spring Boot 4** and **Java 17**, and exposes resources under `/api/v1/` for:

- **Foods** — catalog CRUD  
- **Cart** — cart line items  
- **Orders** — orders and totals  
- **Order items** — line items with optional filter by `orderId`  

Persistence uses **Spring Data JPA** and **MySQL**. Hibernate is configured with `ddl-auto=update` for schema sync in development.

## Setup and installation

### Prerequisites

- **JDK 17** or newer (the project targets Java 17; newer JDKs are typically fine)  
- **Maven** (optional if you use the included wrapper: `./mvnw`)  
- **MySQL** 8+ running locally or reachable from your machine  

### Database

1. Create a database (the default configuration expects a schema named `food_app_db`).  
2. Open `src/main/resources/application.properties` and set:

   - `spring.datasource.url` — JDBC URL for your MySQL instance  
   - `spring.datasource.username` / `spring.datasource.password` — your credentials  

3. Start MySQL before starting the application.

## SQL schema script

Use the following script to create tables manually if needed.

```sql
CREATE TABLE foods (
    id BIGINT NOT NULL AUTO_INCREMENT,
    description VARCHAR(255),
    image_url VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE cart (
    id BIGINT NOT NULL AUTO_INCREMENT,
    food_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    FOREIGN KEY (food_id) REFERENCES foods(id)
);

CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_amount DOUBLE NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price DOUBLE NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (food_id) REFERENCES foods(id)
);
```

### Build

From this directory (`springboot-backend`):

```bash
./mvnw clean package
```

On Windows:

```bash
mvnw.cmd clean package
```

## How to run the application

### Run the API server

```bash
./mvnw spring-boot:run
```

The API listens on **port 8080** by default (`http://localhost:8080`). Example base path: `http://localhost:8080/api/v1/foods`.

### Admin Authentication

username: admin

password: admin123

### Run tests

```bash
./mvnw test
```

Run a single test class:

```bash
./mvnw -Dtest=FoodControllerTest test
```

**Note:** Full application context tests (for example `SpringbootBackendApplicationTests`) need a running MySQL instance that matches `application.properties`. Slice tests such as `@WebMvcTest` controller tests do not require the database.


```
