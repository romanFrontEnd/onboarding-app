# ADR 0005: Resilience of service-a towards service-b

- Status: Accepted
- Date: 2026-11-25
- Change: B2-5095

## Context
AC3: service-a must handle service-b being slow or unavailable in a defined, documented way, demonstrable by scaling service-b to 0.

## Decision
- Timeout `SERVICE_B_TIMEOUT` (milliseconds, default 2000) on connect and read.
- Up to `SERVICE_B_RETRIES` retries (default 2) with a fixed `SERVICE_B_RETRY_BACKOFF` (milliseconds, default 200) between attempts.
- Retry only on timeouts and connection errors. Never on a 4xx or 5xx answer.
- Error mapping: timeout or refused connection gives 503 `SERVICE_B_UNAVAILABLE`; any service-b 4xx or 5xx gives 502 `SERVICE_B_ERROR`; no fallback.
- Worst case when service-b is down: (retries + 1) x timeout + retries x backoff, about 6.4 s with the defaults. Observed with service-b at 0: 503 in under 0.5 s, because the connection is refused at once.

## Rejected
- Retrying 5xx answers: hides real failures and repeats non-idempotent work.
- A fallback payload: it would mask the outage from the consumer.
- SmallRye Fault Tolerance and a circuit breaker: more machinery than a two-service reference needs. A plain loop in `ServiceBClientAdapter` is easier to read and test.

## Consequences
- All values are configuration, changeable per environment without rebuilding.
- The behaviour is covered by scenarios SC-003 to SC-006, SC-016, SC-017, SC-019 and SC-020.
