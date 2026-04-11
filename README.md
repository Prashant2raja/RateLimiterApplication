# API Rate Limiter Service

## Project Overview

The Rate Limiter Application is a Java Spring Boot backend service that protects APIs by limiting request frequency per user, tier, and endpoint. It is built as a middleware-style rate limiting service: the application computes whether each request should be allowed or blocked before the request reaches the protected resource.

This project uses the Token Bucket algorithm as its primary rate limiting strategy. It supports custom rules, tier assignments, manual resets, and runtime status monitoring.

## What This Project Does

- Controls API usage with token bucket throttling
- Protects services from abuse, spikes, and overload
- Supports tier-based plans for `FREE`, `PRO`, `ENTERPRISE`, and `UNLIMITED`
- Allows custom per-user, per-tier, and per-endpoint rules
- Provides a reset endpoint for testing and cleanup
- Exposes status and performance summary endpoints

## Architecture and Project Type

This is a backend service / middleware component:

- Spring Boot REST API application
- Service layer implements rate limiting logic
- Controllers expose management and validation APIs
- Uses in-memory caching for runtime state
- Persists tier assignments to a local file for restart durability

## Technology Stack

- Java 17+ (Spring Boot)
- Spring Web MVC REST
- Caffeine cache for in-memory token buckets
- Java properties file for tier persistence
- Maven build system

## Core Features

- Token Bucket rate limiting
- `429 Too Many Requests` for blocked traffic
- Response headers:
  - `X-RateLimit-Limit`
  - `X-RateLimit-Remaining`
  - `X-RateLimit-Reset`
- Tier assignment API with optional expiry
- Rule engine with priority matching
- Manual bucket reset API
- Runtime status snapshot endpoint
- Performance summary endpoint

## Data Storage and Memory Model

### Memory used

- In-memory token bucket storage using `Caffeine` cache
- Runtime policy cache for resolved identifier/tier/endpoint combinations
- In-memory rule list and tier assignments during app runtime

### Persistent storage

- User tier assignments are stored in `data/tiers.properties`
- This file is loaded on startup and updated when tiers are assigned
- Token buckets themselves are not persisted across restarts

## File Structure and Important Files

Important project files:

- `demo/pom.xml` — Maven project configuration
- `demo/mvnw`, `demo/mvnw.cmd` — Maven wrapper scripts
- `demo/src/main/java/com/demo/RateLimiterApplication.java` — main Spring Boot application
- `demo/src/main/java/com/demo/controller/RateLimitController.java` — rate limit check/reset APIs
- `demo/src/main/java/com/demo/controller/TierController.java` — assign/get tier APIs
- `demo/src/main/java/com/demo/controller/RuleController.java` — add/list custom rules
- `demo/src/main/java/com/demo/controller/StatusController.java` — runtime status API
- `demo/src/main/java/com/demo/controller/PerformanceController.java` — performance summary API
- `demo/src/main/java/com/demo/service/RateLimitService.java` — core token bucket validation and request flow
- `demo/src/main/java/com/demo/service/TierService.java` — tier assignment and tier expiry logic
- `demo/src/main/java/com/demo/service/RuleEngineService.java` — custom rule matching and priority ordering
- `demo/src/main/java/com/demo/service/TokenBucketManager.java` — in-memory token bucket cache manager
- `demo/src/main/java/com/demo/config/TierConfiguration.java` — default tier limits and refill rates
- `demo/src/main/java/com/demo/dto/RateLimitRequest.java` — JSON body for check/reset APIs
- `demo/src/main/resources/application.properties` — application configuration
- `data/tiers.properties` — persisted tier assignments
- `postman/RateLimiter.postman_collection.json` — Postman collection for testing
- `load-tests/rate-limit.js` — load-testing script
- `docs/benchmark-results.md`, `docs/profiling-analysis.md`, `docs/phase3-performance.md` — performance documentation

## API Endpoints

### 1. `GET /api/status`

Returns a snapshot of application health, number of assigned tiers, active token buckets, rule count, and tier configuration.

### 2. `POST /api/rate-limit/check`

Checks whether a request is allowed. Example request body:

```json
{
  "identifier": "demoUser",
  "endpoint": "/api/demo"
}
```

Successful response headers:

- `X-RateLimit-Limit`
- `X-RateLimit-Remaining`
- `X-RateLimit-Reset`

If the limit is exceeded, the API returns `429`.

### 3. `POST /api/rate-limit/reset`

Resets the token bucket for the given user identifier.

```json
{
  "identifier": "demoUser",
  "endpoint": "/api/demo"
}
```

### 4. `POST /api/tier/assign`

Assigns a tier to a user, optionally with expiration.

```json
{
  "identifier": "demoUser",
  "tier": "PRO",
  "expiresAtEpochSeconds": null
}
```

If `expiresAtEpochSeconds` is set, the tier will downgrade automatically after expiry.

### 5. `GET /api/tier/{identifier}`

Returns the assigned tier for a user. If no tier exists, returns `FREE`.

### 6. `POST /api/rule/add`

Adds a custom rule that can match by identifier, tier, and endpoint.

Example request body:

```json
{
  "identifier": "user1",
  "tier": "PRO",
  "endpoint": "/api/demo",
  "capacity": 2300,
  "refillRate": 1,
  "priority": 1
}
```

### 7. `GET /api/rule`

Returns all rules currently loaded by the rule engine.

### 8. `GET /api/performance/summary`

Returns runtime performance metrics and summary data.

## Example Postman Test Flow

1. Call `GET /api/status`
2. Assign a tier with `POST /api/tier/assign`
3. Verify tier with `GET /api/tier/{identifier}`
4. Add a custom rule with `POST /api/rule/add`
5. Call `POST /api/rate-limit/check` repeatedly
6. Reset with `POST /api/rate-limit/reset`
7. Review performance with `GET /api/performance/summary`

Import the collection from `postman/RateLimiter.postman_collection.json`.

## Tier Plan Model

This project models subscription tiers rather than payment integration.

- `FREE` — base tier, default for all unknown users
- `PRO` — higher rate limit for paying users
- `ENTERPRISE` — large capacity for enterprise customers
- `UNLIMITED` — bypasses rate limiting

The tier assignment logic is stored in `data/tiers.properties` and loaded on startup.

## Phase Breakdown

### Phase 1 — Core Rate Limiting

- Implemented a token bucket algorithm
- Built request validation and reject flow
- Added response headers for remaining quotas

### Phase 2 — Tier and Rule Support

- Added user tiers and tier fallback
- Built custom rule engine for identifier/tier/endpoint matching
- Added dynamic tier assignment and expiry support

### Phase 3 — Performance and Optimization

- Added Caffeine in-memory cache for token buckets
- Added performance summary and load test artifacts
- Prepared benchmark and profiling documentation

## Important Code Concepts

### Token Bucket Algorithm

The bucket stores a capacity and refill rate. Each request consumes one token if available. Tokens refill over time based on the configured rate.

### Rule Engine

Custom rules are evaluated in priority order. A rule can match on:

- specific `identifier`
- tier value
- API `endpoint`

If no custom rule matches, the service falls back to the tier's default limit.

### Tier Persistence

User tier assignments persist in `data/tiers.properties` using `TierService`.

### In-Memory Cache

`TokenBucketManager` uses Caffeine to retain buckets in memory and evict them after 30 minutes of inactivity.

## Important Methods to Know

- `RateLimitService.checkLimit(identifier, endpoint)` — core entry point for request validation
- `RateLimitService.resetLimit(identifier)` — clears a user’s buckets
- `RuleEngineService.addRule(rule)` — adds custom rule and sorts by priority
- `RuleEngineService.findApplicableRule(identifier, tier, endpoint)` — resolves the highest priority rule
- `TierService.assignTier(identifier, tier, expiresAtEpochSeconds)` — assign tier and persist it
- `TierService.getTier(identifier)` — returns current tier, automatically downgrading expired tiers
- `TokenBucketManager.getBucket(key, capacity, refillRate)` — creates or returns an existing bucket
- `TokenBucketManager.resetAllForUser(identifier)` — removes all in-memory buckets for a user

## How to Optimize in the Future

- Add Redis or distributed cache for token buckets in multi-instance deployments
- Move rule and tier storage to a database instead of `tiers.properties`
- Add authenticated admin APIs for rule and tier management
- Add endpoint-specific rate limiting policies
- Add metrics and Prometheus instrumentation
- Build an admin dashboard for monitoring and configuration

## Run and Validate

Run the app locally:

```powershell
./mvnw.cmd spring-boot:run
```

Run tests:

```powershell
./mvnw.cmd clean test
```

Base service URL:

```text
http://localhost:8080
```
