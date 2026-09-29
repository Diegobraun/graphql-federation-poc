package br.com.poc.federation.produtos;

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
@WithMockUser(username = "cli-001")
class ProdutoGraphQlTest {

    @Autowired
    private ExecutionGraphQlServiceTester tester;

    @Test
    void filtraCatalogoPorCategoria() {
        tester.document("{ produtos(categoria: INVESTIMENTO) { codigo categoria } }")
                .execute()
                .path("produtos[*].categoria").entityList(String.class).hasSize(5).containsExactly(
                        "INVESTIMENTO", "INVESTIMENTO", "INVESTIMENTO", "INVESTIMENTO", "INVESTIMENTO");
    }

    @Test
    void resolveEntidadesEmLotePreservandoOrdemEAusentes() {
        tester.document("""
                        query($r: [_Any!]!) { _entities(representations: $r) { ... on Produto { codigo nome } } }
                        """)
                .variable("r", List.of(
                        Map.of("__typename", "Produto", "codigo", "LCI-90"),
                        Map.of("__typename", "Produto", "codigo", "NAO-EXISTE"),
                        Map.of("__typename", "Produto", "codigo", "EMP-PESSOAL")))
                .execute()
                .path("_entities[0].codigo").entity(String.class).isEqualTo("LCI-90")
                .path("_entities[1]").valueIsNull()
                .path("_entities[2].nome").entity(String.class).isEqualTo("Empréstimo Pessoal");
    }
}
