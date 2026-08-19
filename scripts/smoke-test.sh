#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
CATALOG_MOCK_URL="${CATALOG_MOCK_URL:-http://localhost:8081}"
WEBHOOK_MOCK_URL="${WEBHOOK_MOCK_URL:-http://localhost:8082}"
ORDER_ID="${ORDER_ID:-craft-smoke-001}"

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

curl -fsS -X POST "$BASE_URL/api/orders/$ORDER_ID/fulfill" >/dev/null
RESPONSE="$(curl -fsS -X POST "$BASE_URL/api/orders/$ORDER_ID/fulfill")"
echo "$RESPONSE"

grep -q '"catalogSource":"catalog-mock"' <<<"$RESPONSE"
grep -q '"stateSource":"redis"' <<<"$RESPONSE"

# Capture the complete WireMock responses before matching them. With pipefail enabled,
# `curl | grep -q` can make curl exit 23 when grep closes the pipe after an early match.
CATALOG_REQUESTS="$(curl -fsS "$CATALOG_MOCK_URL/__admin/requests")"
WEBHOOK_REQUESTS="$(curl -fsS "$WEBHOOK_MOCK_URL/__admin/requests")"
grep -q "/catalog/$ORDER_ID" <<<"$CATALOG_REQUESTS"
grep -q "/fulfillments/$ORDER_ID" <<<"$WEBHOOK_REQUESTS"

echo 'CRAFT golden demo smoke test passed.'
