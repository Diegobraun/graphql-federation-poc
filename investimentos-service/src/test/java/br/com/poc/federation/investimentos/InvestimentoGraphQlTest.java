package br.com.poc.federation.investimentos;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureGraphQlTester
class InvestimentoGraphQlTest {

    @Autowired
    private ExecutionGraphQlServiceTester tester;

    @Test
    @WithMockUser(username = "cli-001")
    void carteiraConsolidaAsPosicoesDoCliente() {
        var resposta = tester.document("""
                        query($r: [_Any!]!) {
                          _entities(representations: $r) {
                            ... on Cliente {
                              investimentos { valorAtual }
                              carteira { patrimonioBruto quantidadeAtivos alocacao { percentual } }
                            }
                          }
                        }
                        """)
                .variable("r", List.of(Map.of("__typename", "Cliente", "id", "cli-001")))
                .execute();

        List<BigDecimal> valores = resposta.path("_entities[0].investimentos[*].valorAtual").entityList(BigDecimal.class).get();
        resposta.path("_entities[0].carteira.quantidadeAtivos").entity(Integer.class).isEqualTo(valores.size());
        resposta.path("_entities[0].carteira.patrimonioBruto").entity(BigDecimal.class)
                .satisfies(total -> assertThat(total).isEqualByComparingTo(valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)));
        resposta.path("_entities[0].carteira.alocacao[*].percentual").entityList(BigDecimal.class)
                .satisfies(p -> assertThat(p.stream().reduce(BigDecimal.ZERO, BigDecimal::add)).isBetween(new BigDecimal("99.9"), new BigDecimal("100.1")));
    }

    @Test
    @WithMockUser(username = "operador", authorities = "SCOPE_backoffice")
    void backofficeVeVolumeAplicadoPorProduto() {
        tester.document("""
                        query($r: [_Any!]!) { _entities(representations: $r) { ... on Produto { volumeAplicado } } }
                        """)
                .variable("r", List.of(Map.of("__typename", "Produto", "codigo", "CDB-2A")))
                .execute()
                .path("_entities[0].volumeAplicado").entity(BigDecimal.class)
                .satisfies(volume -> assertThat(volume).isPositive());
    }
}
