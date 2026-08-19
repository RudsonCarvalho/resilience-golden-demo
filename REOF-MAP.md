# REOF MS-1.1 evidence map

This repository is intentionally a **golden structural-resilience fixture** for `reof-audit`.

This file is only a navigation aid. A compliant auditor must still discover every interaction point itself, inspect code/configuration, resolve effective values, and attach file/line evidence. Nothing should be scored merely because it is mentioned here.

## Architectural boundary

The Java package tree intentionally separates concerns:

```text
domain       business concepts only
application  use case + outbound ports
infra        adapters, resilience mechanisms and runtime technology
```

The REOF mechanisms therefore sit at concrete infrastructure boundaries instead of being mixed into the business model.

## Service boundary

- Independently deployable application service: `reof-golden-demo`
- Functional capability: order fulfillment
- Public route count: 1
- Expected functional domains: `D=1`
- WireMock containers: external-dependency test harnesses, not additional application services
- Redis/Kafka: infrastructure dependencies, not application services

## Interaction-point inventory

| Point | Vertical | Target | Primary evidence location | Intended mechanisms |
|---|---|---|---|---|
| EE-1 | EE | `POST /api/orders/{orderId}/fulfill` | `infra/entrypoint/web/FulfillmentController.java` | semaphore bulkhead |
| CE-1 | CE | catalog `GET` | `infra/client/catalog/CatalogHttpClient.java` + `infra/config/HttpClientsConfig.java` | circuit breaker, local fallback, exponential retry+jitter+cap, global retry token bucket, explicit timeout, connection pool |
| SE-1 | SE | fulfillment webhook `PUT` | `infra/client/webhook/FulfillmentWebhookHttpClient.java` + `infra/config/HttpClientsConfig.java` | circuit breaker, local fallback, exponential retry+jitter+cap, global retry token bucket, explicit timeout, pool, idempotent write, gzip, bounded async dispatch |
| DI-1 | DI | Redis order-state store | `infra/persistence/redis/RedisOrderStateAdapter.java` + `application.yml` + `compose.yaml` | master/replica replication, Sentinel, local fallback, command/connect timeout, Lettuce pool |
| AC-1 | AC | Spring/Kubernetes container | `infra/health/*` + `k8s/deployment.yaml` + `application.yml` | specific readiness, specific liveness, self-healing, graceful shutdown/draining, resource request+limit, distinct startup probe, PDB |
| SE-KAFKA-1 | SE-KAFKA | `fulfillment-events` producer | `infra/messaging/kafka/*` + `application.yml` | Avro schema validation, explicit bounded failure path, throttling, producer idempotence, bounded batch, `acks=all` |

All Java paths in this table are relative to `src/main/java/io/github/rudsoncarvalho/reof/`.

## Key resolved values the auditor should find

### EE

```text
entryBulkhead.maxConcurrentCalls = 32
entryBulkhead.maxWaitDuration = 0ms
```

### CE / SE HTTP transport

```text
connect timeout = 200ms
response/socket timeout = 700ms
connection-request timeout = 150ms
max pooled connections = 32
max connections per route = 16
```

### CE / SE retry

```text
maxAttempts = 3
initial wait = 100ms
exponential multiplier = 2.0
randomized wait = true
randomized factor = 0.5
maximum exponential wait = 500ms
shared global token bucket capacity = 20
shared global refill = 10 tokens/second
```

### Circuit breakers

```text
sliding window = 10 calls
minimum calls = 5
failure threshold = 50%
half-open permitted calls = 3
open-state wait = 5s
```

### DI

```text
Redis master = reofmaster via Sentinel
replica count controlled by compose = 1
Redis connect timeout = 200ms
Redis command timeout = 250ms
Lettuce max active = 8
Lettuce max wait = 250ms
```

### SE-KAFKA

```text
acks = all
enable.idempotence = true
batch.size = 16384
producer throttle = 50/second
failure buffer maximum = 100 records
```

### AC

```text
Spring shutdown = graceful
shutdown phase timeout = 20s
Kubernetes termination grace = 30s
preStop delay = 5s
replicas = 2
CPU request/limit = 100m / 500m
memory request/limit = 192Mi / 384Mi
PDB minAvailable = 1
```

## Anti-pattern avoidance

- No timeout inversion is encoded in-repo; the transport timeouts are explicit and there is no shorter in-repo caller timeout wrapping the interactions.
- Both retries have attempt caps, exponential backoff, random jitter, maximum wait, and a shared global token-bucket retry budget.
- The webhook write is `PUT` plus an explicit `Idempotency-Key`.
- Circuit breakers use a 10-call sliding window, minimum 5 calls and 50% threshold; they can trip under realistic demo traffic.
- HTTP fallbacks return local values and never target the failed backend or a shared backend.
- DI fallback uses process-local state rather than retrying Redis.
- Liveness evaluates an actual critical executor and can return DOWN.
- Every explicit HTTP/Redis connection pool is paired with explicit timeouts.

## Expected score under the supplied profile

The supplied profile declares CE max 15 although its listed mechanisms can sum to 17 when the documented global retry-budget bonus is used. It declares SE max 21 although its listed mechanisms can sum to 23 with that same bonus.

This repository implements all listed mechanisms instead of hiding that profile inconsistency.

| Vertical | Implemented sum | Declared max |
|---|---:|---:|
| EE | 5 | 5 |
| CE | 17 | 15 |
| SE | 23 | 21 |
| DI | 14 | 14 |
| AC | 18 | 18 |
| SE-KAFKA | 17 | 17 |
| **Total** | **94** | **90** |

`D=1`, therefore the domain degradation factor is `1.0`.

With uncapped mechanism sums, `Index=94` exceeds `Index_max=90`, so the required final clamp produces **IRC 10.0 / Excellent**. If an implementation caps every vertical at its declared maximum before normalization, `Index=Index_max=90`, which also produces **IRC 10.0 / Excellent**.

Expected penalty count: `0`.
Expected unverified scored mechanisms after a complete audit: `0`.
