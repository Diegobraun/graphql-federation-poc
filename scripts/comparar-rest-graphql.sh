#!/usr/bin/env bash
set -euo pipefail

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
CLIENTE="${1:-cli-005}"
TOKEN="$("$RAIZ/scripts/gerar-token.sh" "$CLIENTE" cliente)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

for _ in 1 2 3; do
  for url in clientes/$CLIENTE:9101 clientes/$CLIENTE/contas:9102 clientes/$CLIENTE/emprestimos:9103 clientes/$CLIENTE/investimentos:9104 produtos/EMP-PESSOAL:9105; do
    curl -s -o /dev/null -H "authorization: Bearer $TOKEN" "http://localhost:${url##*:}/api/v1/${url%:*}"
  done
  jq -n --rawfile q "$RAIZ/consultas/01-home-app.graphql" '{query: $q}' | curl -s -o /dev/null http://localhost:4000/graphql \
    -H "authorization: Bearer $TOKEN" -H 'content-type: application/json' --data-binary @-
done

REQUISICOES=0
BYTES=0
TEMPO=0

chamar() {
  local nome="$1" url="$2" saida="$TMP/$1.json"
  local metricas
  metricas=$(curl -s -o "$saida" -w '%{size_download} %{time_total}' -H "authorization: Bearer $TOKEN" "$url")
  local bytes="${metricas% *}" tempo="${metricas#* }"
  REQUISICOES=$((REQUISICOES + 1))
  BYTES=$((BYTES + bytes))
  TEMPO=$(echo "$TEMPO + $tempo" | bc)
  printf '  %-48s %8s bytes  %6.0f ms\n' "GET ${url#http://localhost:}" "$bytes" "$(echo "$tempo * 1000" | bc)"
}

echo
echo "Tela inicial do app para $CLIENTE"
echo "Dados usados na tela: nome, segmento, saldo das contas, empréstimos ativos (nome do produto,"
echo "saldo devedor, próxima parcela) e patrimônio investido."
echo
echo "REST (como é hoje)"
chamar cliente "http://localhost:9101/api/v1/clientes/$CLIENTE"
chamar contas "http://localhost:9102/api/v1/clientes/$CLIENTE/contas"
chamar emprestimos "http://localhost:9103/api/v1/clientes/$CLIENTE/emprestimos"
chamar investimentos "http://localhost:9104/api/v1/clientes/$CLIENTE/investimentos"
for codigo in $(jq -r '[.[] | select(.status == "ATIVO") | .emprestimo.produtoCodigo] | unique | .[]' "$TMP/emprestimos.json"); do
  chamar "produto-$codigo" "http://localhost:9105/api/v1/produtos/$codigo"
done
REST_REQ=$REQUISICOES
REST_BYTES=$BYTES
REST_TEMPO=$TEMPO

echo
echo "GraphQL Federation (uma chamada ao router)"
REQUISICOES=0
BYTES=0
TEMPO=0
CORPO=$(jq -n --rawfile q "$RAIZ/consultas/01-home-app.graphql" '{query: $q}')
metricas=$(curl -s -o "$TMP/graphql.json" -w '%{size_download} %{time_total}' http://localhost:4000/graphql \
  -H "authorization: Bearer $TOKEN" -H 'content-type: application/json' --data-binary "$CORPO")
GQL_BYTES="${metricas% *}"
GQL_TEMPO="${metricas#* }"
printf '  %-48s %8s bytes  %6.0f ms\n' "POST 4000/graphql (HomeApp)" "$GQL_BYTES" "$(echo "$GQL_TEMPO * 1000" | bc)"

echo
printf "%12s %12s\n" "REST" "GraphQL"
printf "%12s %12s   %s\n" "$REST_REQ" "1" "requisições do cliente"
printf "%12s %12s   %s\n" "2" "1" "round-trips sequenciais"
printf "%12s %12s   %s\n" "$REST_BYTES" "$GQL_BYTES" "bytes trafegados"
printf "%12.0f %12.0f   %s\n" "$(echo "$REST_TEMPO * 1000" | bc)" "$(echo "$GQL_TEMPO * 1000" | bc)" "ms somados"
echo
echo "Redução de payload: $(echo "scale=1; 100 - ($GQL_BYTES * 100 / $REST_BYTES)" | bc)%"
