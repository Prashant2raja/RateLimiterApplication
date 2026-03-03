# 🚀 API Rate Limiter Service

## 📌 Project Overview

This project implements a scalable **API Rate Limiting Service** using the **Token Bucket algorithm**.

It controls how many requests a user / IP / API key can make within a defined rate limit.

The system supports:

- Tier-based rate limiting (FREE / PRO / ENTERPRISE / UNLIMITED)
- Per-endpoint rate limiting rules
- Rule priority handling
- HTTP 429 responses when limit exceeded
- Rate limit response headers
- Unit testing with 10 test cases

---

# ✅ Phase 1 – Core Implementation

### ✔ Features Implemented

- Token Bucket algorithm
- In-memory storage using `ConcurrentHashMap`
- Separate bucket per identifier + endpoint
- Thread-safe implementation
- REST API endpoints:
  - `POST /api/rate-limit/check`
  - `POST /api/rate-limit/reset`
- Returns **HTTP 429 (Too Many Requests)** when limit exceeded
- Reset endpoint for testing
- Unit tests

---

# ✅ Phase 2 – Feature Extensions

## 1️⃣ Multiple Tier Support

- FREE
- PRO
- ENTERPRISE
- UNLIMITED

Each tier has different capacity and refill rate.

✔ Dynamic tier assignment  
✔ Automatic tier detection during rate check  

---

## 2️⃣ Advanced Rules Engine

Supports:

- Per-endpoint rate limits
- Identifier-specific rules
- Tier-based rules
- Rule priority handling
- Rule override mechanism

Example:

- `/api/search` → 5 requests/sec  
- `/api/upload` → 2 requests/sec  
- Specific user override supported  

---

# 📊 Rate Limit Headers

Every response includes:

- `X-RateLimit-Limit`
- `X-RateLimit-Remaining`
- `X-RateLimit-Reset`

If limit exceeded:

- Returns HTTP 429
- Headers still included

---

# 🛠 Tech Stack

- Java 17  
- Spring Boot  
- Maven  
- JUnit 5  
- ConcurrentHashMap (In-memory storage)

---

# 📁 Project Structure

src
├── main
│ └── java
│ └── com.demo
│ ├── controller
│ ├── service
│ ├── model
│ ├── config
│ └── dto
│
└── test
└── java
└── com.demo.service
└── RateLimitServiceTest.java


---

# ▶️ How to Run

Build the project:


mvn clean install


Run the application:


mvn spring-boot:run


Application runs on:


http://localhost:8080


---

# 🧪 API Usage

## Check Rate Limit

**POST**


/api/rate-limit/check


Body:

```json
{
  "identifier": "user1",
  "endpoint": "/api/search"
}
Reset Rate Limit

POST

/api/rate-limit/reset

Body:

{
  "identifier": "user1"
}
🧪 Run Tests
mvn test

✔ 10 Unit Test Cases Implemented
✔ Rule override tested
✔ Tier fallback tested
✔ Endpoint isolation tested
✔ Reset functionality tested
✔ Rule priority handling tested

⚙️ Performance Notes

O(1) lookup using ConcurrentHashMap

Thread-safe TokenBucket

Endpoint-based bucket isolation

Lightweight and extensible

Ready for Redis-based distributed extension (Phase 3)

📌 Status

✔ Phase 1 Completed
✔ Phase 2 Completed

