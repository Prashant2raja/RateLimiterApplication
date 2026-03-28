import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    steady_10k: {
      executor: 'constant-arrival-rate',
      rate: 10000,
      timeUnit: '1s',
      duration: '30s',
      preAllocatedVUs: 300,
      maxVUs: 1200,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<10', 'p(99)<50'],
  },
};

const payload = JSON.stringify({
  identifier: 'perf-user',
  endpoint: '/api/search',
});

export default function () {
  const response = http.post('http://localhost:8080/api/rate-limit/check', payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(response, {
    'status is 200 or 429': (r) => r.status === 200 || r.status === 429,
    'rate limit headers present': (r) =>
      r.headers['X-RateLimit-Limit'] &&
      r.headers['X-RateLimit-Remaining'] &&
      r.headers['X-RateLimit-Reset'],
  });
}
