#!/usr/bin/env bash
set -euo pipefail

if [ $# -lt 1 ]; then
  echo "uso: $0 <arquivo.graphql> [variaveis-json] [sub] [escopo]" >&2
  echo "ex.: $0 consultas/01-home-app.graphql '{}' cli-003 cliente" >&2
  exit 1
fi

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
ARQUIVO="$1"
VARIAVEIS="${2:-}"
[ -n "$VARIAVEIS" ] || VARIAVEIS='{}'
TOKEN="$("$RAIZ/scripts/gerar-token.sh" "${3:-cli-003}" "${4:-cliente}")"
ROUTER="${ROUTER_URL:-http://localhost:4000/graphql}"

jq -n --rawfile q "$ARQUIVO" --argjson v "$VARIAVEIS" '{query: $q, variables: $v}' |
  curl -s "$ROUTER" \
    -H "authorization: Bearer $TOKEN" \
    -H 'content-type: application/json' \
    -H "apollographql-client-name: ${CLIENT_NAME:-cli-consultar}" \
    ${PLANO:+-H 'Apollo-Expose-Query-Plan: true'} \
    --data-binary @- | jq .
