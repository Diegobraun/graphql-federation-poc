package br.com.poc.federation.emprestimos;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
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
class EmprestimoGraphQlTest {

    private static final String OFERTA = """
            query($r: [_Any!]!) {
              _entities(representations: $r) {
                ... on Cliente { ofertaCredito { limitePreAprovado taxaJurosMensal motivo produto { codigo } } comprometimentoRenda }
              }
            }
            """;

    @Autowired
    private ExecutionGraphQlServiceTester tester;

    @Test
    @WithMockUser(username = "cli-001")
    void requiresUsaRendaEScoreRecebidosNaRepresentacao() {
        var representacao = Map.<String, Object>of("__typename", "Cliente", "id", "cli-001", "rendaMensal", 20000, "scoreCredito", 850);
        tester.document(OFERTA).variable("r", List.of(representacao)).execute()
                .path("_entities[0].ofertaCredito.taxaJurosMensal").entity(BigDecimal.class).isEqualTo(new BigDecimal("1.49"))
                .path("_entities[0].ofertaCredito.limitePreAprovado").entity(BigDecimal.class)
                .satisfies(limite -> assertThat(limite).isLessThanOrEqualTo(new BigDecimal("160000")))
                .path("_entities[0].ofertaCredito.produto.codigo").entity(String.class).isEqualTo("EMP-PESSOAL");
    }

    @Test
    @WithMockUser(username = "cli-001")
    void scoreBaixoNaoRecebeOferta() {
        var representacao = Map.<String, Object>of("__typename", "Cliente", "id", "cli-001", "rendaMensal", "5000.00", "scoreCredito", 320);
        tester.document(OFERTA).variable("r", List.of(representacao)).execute()
                .path("_entities[0].ofertaCredito.limitePreAprovado").entity(BigDecimal.class)
                .satisfies(limite -> assertThat(limite).isZero())
                .path("_entities[0].ofertaCredito.motivo").entity(String.class).isEqualTo("Score abaixo da política de crédito");
    }

    @Test
    @WithMockUser(username = "cli-001")
    void simulacaoSegueTabelaPrice() {
        var resposta = tester.document("""
                        {
                          simularEmprestimo(input: { produtoCodigo: "EMP-CONSIGNADO", valor: 12000, quantidadeParcelas: 24 }) {
                            valorParcela totalPago totalJuros taxaJurosMensal
                            parcelas { numero valor amortizacao juros }
                          }
                        }
                        """)
                .execute();
        List<BigDecimal> amortizacoes = resposta.path("simularEmprestimo.parcelas[*].amortizacao").entityList(BigDecimal.class).hasSize(24).get();
        assertThat(amortizacoes.stream().reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo("12000");
        resposta.path("simularEmprestimo.valorParcela").entity(BigDecimal.class).isEqualTo(new BigDecimal("619.46"));
    }

    @Test
    @WithMockUser(username = "cli-001")
    void simulacaoRejeitaProdutoQueNaoEEmprestimo() {
        tester.document("{ simularEmprestimo(input: { produtoCodigo: \"CDB-2A\", valor: 1000, quantidadeParcelas: 12 }) { valorParcela } }")
                .execute()
                .errors()
                .expect(e -> e.getErrorType() == ErrorType.BAD_REQUEST && e.getMessage().contains("CDB-2A"))
                .verify();
    }

    @Test
    @WithMockUser(username = "cli-001")
    void indicadorDeProdutoEhRestritoAoBackoffice() {
        tester.document("""
                        query($r: [_Any!]!) { _entities(representations: $r) { ... on Produto { codigo contratosAtivos } } }
                        """)
                .variable("r", List.of(Map.of("__typename", "Produto", "codigo", "EMP-PESSOAL")))
                .execute()
                .errors()
                .expect(e -> e.getErrorType() == ErrorType.FORBIDDEN)
                .verify()
                .path("_entities[0].codigo").entity(String.class).isEqualTo("EMP-PESSOAL")
                .path("_entities[0].contratosAtivos").valueIsNull();
    }
}
