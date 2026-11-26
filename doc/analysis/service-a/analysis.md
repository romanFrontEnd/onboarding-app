# MS service-a: Living Functional Analysis

Whole-service analysis for `service-a`. It accumulates the development of the service. Change-level detail lives in `doc/spec/<change-id>/spec.md`.

## Change Log
| Version | Date | Change Description | Change | Author |
|---------|------|--------------------|--------|--------|
| 0.1 | 2026-11-25 | Initial analysis from B2-5095 | B2-5095 | Roman |

## Service Information

### Description
`service-a` is the caller service of the B2-5095 reference pair. It exposes `GET /api/v1/combined?name={name}`, calls `service-b` over HTTP and returns the combined result. (Confidence: High)

### Usage Context
Called by a developer or test client after `make up`, through `kubectl port-forward`.

### Behaviour summary
- Validates `name` (non-blank, at most 50 characters), otherwise 400 `INVALID_REQUEST`.
- Calls `service-b` at `SERVICE_B_URL` with `SERVICE_B_TIMEOUT` (default 2s) and `SERVICE_B_RETRIES` (default 2) with backoff (Low confidence on the defaults, to confirm). Retries only on timeouts and connection errors, never on 4xx or 5xx.
- Worst-case response time when B is down: (retries + 1) x timeout plus backoff.
- Propagates `X-Transaction-Id` to B and logs it, generating one if absent.
- Returns 503 `SERVICE_B_UNAVAILABLE` on timeout or connection failure, and 502 `SERVICE_B_ERROR` on a B 5xx.
- Readiness does not depend on B. Liveness and readiness are on `/q/health/live` and `/q/health/ready`.

### Backend
| Backend | Type | Environments |
|---|---|---|
| service-b | service Rest | `local`, `example` (overlay) |

## Open Questions
1. Keep timeout 2s and 2 retries?
2. Is correlation-ID tracing enough, or is OpenTelemetry wanted?

## Source
- `doc/spec/B2-5095/spec.md` (v0.6, approved at the spec-approved gate by the Developer, Analyst approval pending)
