#!/usr/bin/env bash
set -uo pipefail

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
ROUTER="${ROUTER_URL:-http://localhost:4000/graphql}"
FALHAS=0

token() { "$RAIZ/scripts/gerar-token.sh" "$1" "${2:-cliente}"; }

consultar() {
  local token="$1" query="$2" variaveis="${3:-}"
  [ -n "$variaveis" ] || variaveis='{}'
  jq -n --arg q "$query" --argjson v "$variaveis" '{query: $q, variables: $v}' |
    curl -s "$ROUTER" -H "authorization: Bearer $token" -H 'content-type: application/json' ${4:+-H "$4"} --data-binary @-
}

arquivo() { cat "$RAIZ/consultas/$1"; }

verificar() {
  local descricao="$1" resposta="$2" filtro="$3"
  if echo "$resposta" | jq -e "$filtro" >/dev/null 2>&1; then
    printf '  ok     %s\n' "$descricao"
  else
    printf '  FALHOU %s\n         resposta: %s\n' "$descricao" "$(echo "$resposta" | head -c 400)"
    FALHAS=$((FALHAS + 1))
  fi
}

CLIENTE=$(token cli-005)
OUTRO=$(token cli-001)
BACKOFFICE=$(token operador-01 backoffice)

echo "Testes ponta a ponta contra $ROUTER"

STATUS=$(curl -s -o /dev/null -w '%{http_code}' "$ROUTER" -H 'content-type: application/json' -d '{"query":"{ me { id } }"}')
verificar "requisição sem token é barrada no router (401)" "{\"s\":$STATUS}" '.s == 401'

R=$(consultar "$CLIENTE" "$(arquivo 01-home-app.graphql)")
verificar "home do app junta 5 subgraphs em uma chamada" "$R" '(.errors | not) and .data.me.nome != null and (.data.me.contas | length) > 0 and .data.me.carteira != null and (.data.me.emprestimos[0].produto.nome | type) == "string"'

R=$(consultar "$CLIENTE" "$(arquivo 02-oferta-credito.graphql)")
verificar "@requires: oferta usa renda e score do subgraph clientes" "$R" '(.errors | not) and .data.me.ofertaCredito.motivo != null and (.data.me.comprometimentoRenda | type) == "number"'

R=$(consultar "$CLIENTE" '{ me { nome scoreCredito } }')
verificar "@inaccessible: scoreCredito não existe na API pública" "$R" '.errors[0].extensions.code == "GRAPHQL_VALIDATION_FAILED"'

R=$(consultar "$CLIENTE" "$(arquivo 03-extrato-paginado.graphql)" '{"contaId":"cta-005-1"}')
CURSOR=$(echo "$R" | jq -r '.data.conta.extrato.pageInfo.endCursor')
R2=$(consultar "$CLIENTE" "$(arquivo 03-extrato-paginado.graphql)" "{\"contaId\":\"cta-005-1\",\"after\":\"$CURSOR\"}")
verificar "extrato paginado por cursor sem repetir lançamentos" "$(jq -n --argjson a "$R" --argjson b "$R2" '{a: $a, b: $b}')" \
  '([.a.data.conta.extrato.edges[].node.dataHora] - [.b.data.conta.extrato.edges[].node.dataHora] | length) == 5'

R=$(consultar "$OUTRO" "$(arquivo 03-extrato-paginado.graphql)" '{"contaId":"cta-005-1"}')
verificar "cliente não lê conta de outro cliente (FORBIDDEN)" "$R" '.errors[0].extensions.classification == "FORBIDDEN" and .data.conta == null'

R=$(consultar "$CLIENTE" "$(arquivo 04-visao-360-backoffice.graphql)")
verificar "visão 360 negada para escopo cliente" "$R" '.errors[0].extensions.classification == "FORBIDDEN"'

R=$(consultar "$BACKOFFICE" "$(arquivo 04-visao-360-backoffice.graphql)" '{"filtro":{"segmento":"VAREJO"}}')
verificar "visão 360 do backoffice cruza os 5 subgraphs" "$R" '(.errors | not) and .data.clientes.totalCount > 0 and ([.data.clientes.edges[].node.segmento] | unique) == ["VAREJO"]'

R=$(consultar "$CLIENTE" "$(arquivo 06-catalogo-backoffice.graphql)")
verificar "resposta parcial: catálogo volta, indicadores restritos viram null + erro" "$R" \
  '(.data.produtos | length) == 11 and .data.produtos[0].contratosAtivos == null and .errors[0].extensions.classification == "FORBIDDEN"'

R=$(consultar "$CLIENTE" "$(arquivo 05-simulacao.graphql)" '{"input":{"produtoCodigo":"EMP-VEICULO","valor":50000,"quantidadeParcelas":48}}')
verificar "simulação resolve produto no subgraph produtos" "$R" '(.errors | not) and (.data.simularEmprestimo.parcelas | length) == 48 and .data.simularEmprestimo.produto.nome == "Financiamento de Veículo"'

R=$(consultar "$CLIENTE" '{ me { contas { titular { contas { titular { contas { titular { contas { titular { contas { titular { id } } } } } } } } } } } }')
verificar "query profunda demais barrada no router (Rhai)" "$R" '.errors[0].message | test("profundidade")'

R=$(consultar "$CLIENTE" "$(arquivo 01-home-app.graphql)" '{}' 'Apollo-Expose-Query-Plan: true')
verificar "query plan exposto para depuração" "$R" '.extensions.apolloQueryPlan.text | test("Parallel")'

echo
if [ "$FALHAS" -eq 0 ]; then
  echo "Todos os testes passaram."
else
  echo "$FALHAS teste(s) falharam."
  exit 1
fi
