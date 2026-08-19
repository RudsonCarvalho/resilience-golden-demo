#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
ORDER_PREFIX="${ORDER_PREFIX:-craft-failure}"

echo 'Stopping the external catalog and webhook mocks...'
docker compose stop catalog-mock webhook-mock

cleanup() {
  echo 'Restoring external mocks...'
  docker compose start catalog-mock webhook-mock >/dev/null
}
trap cleanup EXIT

for i in 1 2 3; do
  RESPONSE="$(curl -fsS -X POST "$BASE_URL/api/orders/${ORDER_PREFIX}-${i}/fulfill")"
  echo "$RESPONSE"
  grep -q '"catalogSource":"fallback-local"' <<<"$RESPONSE"
done

# The catalog is synchronous, so its fallback is visible in the API response.
# The webhook is deliberately asynchronous; retry/failure activity is visible in Actuator.
sleep 2
echo
echo 'Circuit-breaker state:'
curl -fsS "$BASE_URL/actuator/circuitbreakers" || true
echo
echo 'Recent retry events:'
curl -fsS "$BASE_URL/actuator/retryevents" || true
echo
echo 'Failure-path demo completed without losing the order-fulfillment API.'
