#!/usr/bin/env bash
set -euo pipefail

SUB="${1:-cli-001}"
ESCOPO="${2:-cliente}"
SEGREDO="${JWT_SECRET:-poc-graphql-federation-segredo-de-desenvolvimento-256}"
AGORA=$(date +%s)
EXPIRA=$((AGORA + ${TTL_SEGUNDOS:-3600}))

b64url() { openssl base64 -A | tr '+/' '-_' | tr -d '='; }

CABECALHO=$(printf '{"alg":"HS256","typ":"JWT"}' | b64url)
CORPO=$(printf '{"sub":"%s","scope":"%s","iss":"poc-federation","iat":%d,"exp":%d}' "$SUB" "$ESCOPO" "$AGORA" "$EXPIRA" | b64url)
ASSINATURA=$(printf '%s.%s' "$CABECALHO" "$CORPO" | openssl dgst -sha256 -hmac "$SEGREDO" -binary | b64url)

echo "${CABECALHO}.${CORPO}.${ASSINATURA}"
