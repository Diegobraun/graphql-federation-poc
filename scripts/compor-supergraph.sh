#!/usr/bin/env bash
set -euo pipefail

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
cd "$RAIZ/supergraph"

export APOLLO_ELV2_LICENSE=accept
export APOLLO_TELEMETRY_DISABLED=1

npx -y @apollo/rover@0.41.0 supergraph compose --config supergraph.yaml --output supergraph.graphql
echo "Supergraph gerado em supergraph/supergraph.graphql"
