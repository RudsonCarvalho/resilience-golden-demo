#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
CATALOG_MOCK_URL="${CATALOG_MOCK_URL:-http://localhost:8081}"
WEBHOOK_MOCK_URL="${WEBHOOK_MOCK_URL:-http://localhost:8082}"
ORDER_ID="${ORDER_ID:-reof-smoke-001}"

printf 'Waiting for application readiness'
for _ in $(seq 1 60); do
  if curl -fsS "$BASE_URL/actuator/health/readiness" | grep -q '"status":"UP"'; then
    printf ' OK\n'
    break
  fi
  printf '.'
  sleep 2
done

curl -fsS "$BASE_URL/actuator/health/readiness" | grep -q '"status":"UP"' || {
  echo 'Application never became ready.' >&2
  exit 1
}

# First call creates the state; the second proves Redis can read it back.
curl -fsS -X POST "$BASE_URL/api/orders/$ORDER_ID/fulfill" >/dev/null
RESPONSE="$(curl -fsS -X POST "$BASE_URL/api/orders/$ORDER_ID/fulfill")"
echo "$RESPONSE"

echo "$RESPONSE" | grep -q '"catalogSource":"catalog-mock"'
echo "$RESPONSE" | grep -q '"stateSource":"redis"'

# Prove the two outbound HTTP interactions really happened.
curl -fsS "$CATALOG_MOCK_URL/__admin/requests" | grep -q "/catalog/$ORDER_ID"
curl -fsS "$WEBHOOK_MOCK_URL/__admin/requests" | grep -q "/fulfillments/$ORDER_ID"

echo 'REOF golden demo smoke test passed.'
