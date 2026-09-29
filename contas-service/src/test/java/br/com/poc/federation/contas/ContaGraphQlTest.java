package br.com.poc.federation.contas;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureGraphQlTester
class ContaGraphQlTest {

    private static final String ENTIDADE_CLIENTE = """
            query($r: [_Any!]!) {
              _entities(representations: $r) {
                ... on Cliente { id saldoTotalEmContas contas { id tipo saldo { disponivel total } titular { id } produto { codigo } } }
              }
            }
            """;

    @Autowired
    private ExecutionGraphQlServiceTester tester;

    @Test
    @WithMockUser(username = "operador", authorities = "SCOPE_backoffice")
    void estendeClienteComContasEmLote() {
        var resposta = tester.document(ENTIDADE_CLIENTE)
                .variable("r", List.of(representacao("cli-001"), representacao("cli-002"), representacao("cli-003")))
                .execute();

        resposta.path("_entities[*].id").entityList(String.class).containsExactly("cli-001", "cli-002", "cli-003");
        resposta.path("_entities[0].contas[0].id").entity(String.class).isEqualTo("cta-001-1");
        resposta.path("_entities[0].contas[0].titular.id").entity(String.class).isEqualTo("cli-001");

        List<BigDecimal> saldos = resposta.path("_entities[0].contas[*].saldo.disponivel").entityList(BigDecimal.class).get();
        BigDecimal total = resposta.path("_entities[0].saldoTotalEmContas").entity(BigDecimal.class).get();
        assertThat(total).isEqualByComparingTo(saldos.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Test
    @WithMockUser(username = "cli-001")
    void clienteNaoResolveEntidadeDeOutroCliente() {
        tester.document(ENTIDADE_CLIENTE)
                .variable("r", List.of(representacao("cli-002")))
                .execute()
                .errors()
                .expect(e -> e.getErrorType() == ErrorType.FORBIDDEN)
                .verify();
    }

    @Test
    @WithMockUser(username = "cli-004")
    void extratoPaginadoPercorreTodosOsLancamentosSemRepetir() {
        String consulta = """
                query($after: String) {
                  conta(id: "cta-004-1") {
                    extrato(first: 40, after: $after) {
                      totalCount
                      pageInfo { hasNextPage endCursor }
                      edges { node { id dataHora } }
                    }
                  }
                }
                """;
        List<String> ids = new ArrayList<>();
        String cursor = null;
        boolean temMais = true;
        while (temMais) {
            var pagina = tester.document(consulta).variable("after", cursor).execute();
            ids.addAll(pagina.path("conta.extrato.edges[*].node.id").entityList(String.class).get());
            temMais = pagina.path("conta.extrato.pageInfo.hasNextPage").entity(Boolean.class).get();
            cursor = pagina.path("conta.extrato.pageInfo.endCursor").entity(String.class).get();
        }
        assertThat(ids).hasSize(150).doesNotHaveDuplicates();
    }

    @Test
    @WithMockUser(username = "cli-004")
    void extratoFiltraPorTipo() {
        tester.document("{ conta(id: \"cta-004-1\") { extrato(tipo: CREDITO, first: 100) { edges { node { tipo valor } } } } }")
                .execute()
                .path("conta.extrato.edges[*].node.tipo").entityList(String.class)
                .satisfies(tipos -> assertThat(tipos).isNotEmpty().containsOnly("CREDITO"));
    }

    private static Map<String, Object> representacao(String id) {
        return Map.of("__typename", "Cliente", "id", id);
    }
}
