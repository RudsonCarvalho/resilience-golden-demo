# CRAFT/MS-1.1.1 Evidence Map

This repository is a positive-control fixture for the [CRAFT structural resilience audit](https://github.com/RudsonCarvalho/craft-audit).

This document is a navigation aid, **not scoring evidence**. A compliant CRAFT audit must independently discover each interaction point, resolve effective configuration values, and cite the actual source/configuration lines before awarding points.

## Architectural boundary

```text
domain       business concepts only
application  use case + outbound ports
infra        adapters, resilience mechanisms and runtime technology
```

The resilience mechanisms live at concrete infrastructure boundaries rather than inside the domain model.

## Service boundary

- Deployable application: `resilience-golden-demo`
- Language/runtime: Java 21 / Spring Boot
- Functional capability: order fulfillment
- Public business route count: 1
- Expected functional domains: `D = 1`
- WireMock containers: external-dependency test harnesses, not application services
- Redis and Kafka: infrastructure dependencies, not application services

## Interaction points

| Point | CRAFT vertical | Target | Primary evidence location | Intended mechanisms |
|---|---|---|---|---|
| EE-1 | External Entry | `POST /api/orders/{orderId}/fulfill` | `infra/entrypoint/web/FulfillmentController.java` | semaphore bulkhead |
| CE-1 | External Consultation | catalog `GET` | `infra/client/catalog/CatalogHttpClient.java`, `infra/config/HttpClientsConfig.java` | circuit breaker, local fallback, exponential retry + jitter + cap, global retry budget, timeout, pool |
| SE-1 | External Exit | fulfillment webhook `PUT` | `infra/client/webhook/FulfillmentWebhookHttpClient.java`, `infra/config/HttpClientsConfig.java` | circuit breaker, fallback, retry, retry budget, timeout, pool, idempotent write, gzip, bounded async dispatch |
| DI-1 | Internal Data | Redis order-state store | `infra/persistence/redis/RedisOrderStateAdapter.java`, `application.yml`, `compose.yaml` | replication, Sentinel, fallback, timeout, pool |
| AC-1 | Application Container | Spring/Kubernetes runtime | `infra/health/*`, `k8s/deployment.yaml`, `application.yml` | readiness, liveness, self-healing, graceful shutdown, resources, startup probe, PDB |
| SE-KAFKA-1 | Event Publishing | `fulfillment-events` | `infra/messaging/kafka/*`, `application.yml` | Avro validation, bounded failure path, throttling, idempotence, bounded batch, `acks=all` |

All Java paths above are relative to `src/main/java/io/github/rudsoncarvalho/craft/`.

## Resolved values the auditor should independently find

### Entry protection

```text
entryBulkhead.maxConcurrentCalls = 32
entryBulkhead.maxWaitDuration = 0ms
```

### HTTP transport

```text
connect timeout = 200ms
response/socket timeout = 700ms
connection-request timeout = 150ms
max pooled connections = 32
max connections per route = 16
```

### Retry

```text
maxAttempts = 3
initial wait = 100ms
exponential multiplier = 2.0
randomized wait = true
randomized factor = 0.5
maximum exponential wait = 500ms
shared retry budget capacity = 20
shared retry budget refill = 10 tokens/second
```

### Circuit breakers

```text
sliding window = 10 calls
minimum calls = 5
failure threshold = 50%
half-open permitted calls = 3
open-state wait = 5s
```

### Redis

```text
Sentinel master = craftmaster
replicas controlled by compose = 1
connect timeout = 200ms
command timeout = 250ms
Lettuce max active = 8
Lettuce max wait = 250ms
```

### Kafka

```text
acks = all
enable.idempotence = true
batch.size = 16384
producer throttle = 50/second
failure buffer maximum = 100 records
```

### Container/deployment

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

- No in-repo timeout inversion wraps the outbound calls.
- Retries have attempt caps, exponential backoff, jitter, maximum wait, and a shared token-bucket budget.
- The webhook uses idempotent `PUT` plus an explicit `Idempotency-Key`.
- Circuit breakers use realistic windows and thresholds.
- Fallbacks are local and do not call the failed backend again.
- Liveness executes real runtime logic and can return DOWN.
- Explicit HTTP and Redis pools are paired with explicit timeouts.

## Expected CRAFT result

The active profile is **CRAFT/MS-1.1.1**.

| Vertical | Expected score | Profile maximum |
|---|---:|---:|
| EE | 5 | 5 |
| CE | 17 | 17 |
| SE | 23 | 23 |
| DI | 14 | 14 |
| AC | 18 | 18 |
| SE-KAFKA | 17 | 17 |
| **Total** | **94** | **94** |

With `D = 1`, the degradation factor is `1.0`. Expected penalties: `0`; expected unverified scored mechanisms: `0`; expected **Resilience Score: 10.0 — Excelente**.
