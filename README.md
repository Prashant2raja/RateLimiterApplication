# API Rate Limiter Service

This project implements a token bucket rate limiter in Spring Boot and covers:

- Phase 1: core token bucket rate limiting
- Phase 2: multiple subscription tiers
- Phase 3: high-performance optimization and measurement

## Implemented Features

- Token bucket algorithm with atomic operations
- HTTP `429 Too Many Requests` handling
- `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset` headers
- Manual reset endpoint for testing
- In-memory bucket storage with efficient lookup
- Tier support: `FREE`, `PRO`, `ENTERPRISE`, `UNLIMITED`
- Optional tier expiry with automatic fallback to `FREE`
- Rule engine with identifier, tier, endpoint, and priority matching
- Status endpoint for runtime health and configuration visibility
- Performance summary endpoint
- Unit tests and benchmark test
- Postman collection

## Endpoints

### Status

`GET /api/status`

Returns application status, active rule count, assigned tier count, active buckets, cached policies, and tier configuration.

### Check Rate Limit

`POST /api/rate-limit/check`

```json
{
  "identifier": "demoUser",
  "endpoint": "/api/demo"
}
```

### Reset Rate Limit

`POST /api/rate-limit/reset`

```json
{
  "identifier": "demoUser",
  "endpoint": "/api/demo"
}
```

### Assign Tier

`POST /api/tier/assign`

```json
{
  "identifier": "demoUser",
  "tier": "PRO",
  "expiresAtEpochSeconds": null
}
```

If `expiresAtEpochSeconds` is set to a future Unix timestamp, the tier automatically downgrades to `FREE` after expiry.

### Get Tier

`GET /api/tier/demoUser`

### Add Rule

`POST /api/rule/add`

```json
{
  "identifier": "demoUser",
  "tier": "PRO",
  "endpoint": "/api/demo",
  "capacity": 2,
  "refillRate": 1,
  "priority": 1
}
```

### List Rules

`GET /api/rule`

### Performance Summary

`GET /api/performance/summary`

## Default Tier Limits

Configured in [application.properties](src/main/resources/application.properties):

- `FREE`: `100` tokens, refill `100/hour`
- `PRO`: `1000` tokens, refill `1000/hour`
- `ENTERPRISE`: `5000` tokens, refill `5000/hour`
- `UNLIMITED`: effectively unlimited

## How To Run

Run tests:

```powershell
./mvnw.cmd clean test
```

Run the application:

```powershell
./mvnw.cmd spring-boot:run
```

Base URL:

```text
http://localhost:8080
```

## Postman

Import:

[`postman/RateLimiter.postman_collection.json`](postman/RateLimiter.postman_collection.json)

Suggested execution order:

1. `Status`
2. `Assign Tier`
3. `Get Tier`
4. `Add Rule`
5. `List Rules`
6. `Check Rate Limit`
7. `Reset Rate Limit`
8. `Performance Summary`

## Tests Included

- basic request allowance
- rule-based blocking
- reset behavior
- endpoint isolation
- user isolation
- tier fallback
- remaining token tracking
- rule priority handling
- cache refresh after rule changes
- benchmark comparison for token bucket implementations

## Performance Assets

- [load-tests/rate-limit.js](load-tests/rate-limit.js)
- [docs/benchmark-results.md](docs/benchmark-results.md)
- [docs/profiling-analysis.md](docs/profiling-analysis.md)
- [docs/phase3-performance.md](docs/phase3-performance.md)
