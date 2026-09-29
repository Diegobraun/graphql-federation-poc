package br.com.poc.federation.clientes;

import static org.assertj.core.api.Assertions.assertThat;

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
class ClienteGraphQlTest {

    @Autowired
    private ExecutionGraphQlServiceTester tester;

    @Test
    @WithMockUser(username = "cli-003")
    void meRetornaClienteDoToken() {
        tester.document("{ me { id nome segmento idade endereco { uf } } }")
                .execute()
                .path("me.id").entity(String.class).isEqualTo("cli-003")
                .path("me.nome").entity(String.class).satisfies(nome -> assertThat(nome).startsWith("Carla"));
    }

    @Test
    @WithMockUser(username = "cli-003")
    void clienteNaoAcessaDadosDeOutroCliente() {
        tester.document("{ cliente(id: \"cli-001\") { nome } }")
                .execute()
                .errors()
                .expect(e -> e.getErrorType() == ErrorType.FORBIDDEN && e.getMessage().contains("cli-001"))
                .verify()
                .path("cliente").valueIsNull();
    }

    @Test
    @WithMockUser(username = "cli-003")
    void listagemExigeBackoffice() {
        tester.document("{ clientes { totalCount } }")
                .execute()
                .errors()
                .expect(e -> e.getErrorType() == ErrorType.FORBIDDEN)
                .verify();
    }

    @Test
    @WithMockUser(username = "operador", authorities = "SCOPE_backoffice")
    void backofficePaginaPorCursorSemRepetirClientes() {
        String consulta = """
                query($after: String) {
                  clientes(first: 10, after: $after) {
                    totalCount
                    pageInfo { hasNextPage endCursor }
                    edges { node { id } }
                  }
                }
                """;
        var primeira = tester.document(consulta).execute();
        primeira.path("clientes.totalCount").entity(Integer.class).isEqualTo(30);
        primeira.path("clientes.pageInfo.hasNextPage").entity(Boolean.class).isEqualTo(true);
        List<String> idsPrimeira = primeira.path("clientes.edges[*].node.id").entityList(String.class).get();
        String cursor = primeira.path("clientes.pageInfo.endCursor").entity(String.class).get();

        List<String> idsSegunda = tester.document(consulta).variable("after", cursor).execute()
                .path("clientes.edges[*].node.id").entityList(String.class).get();

        assertThat(idsPrimeira).hasSize(10).doesNotContainAnyElementsOf(idsSegunda);
        assertThat(idsSegunda).first().isEqualTo("cli-011");
    }

    @Test
    @WithMockUser(username = "cli-007")
    void resolveEntidadeParaOutrosSubgraphs() {
        tester.document("""
                        query($r: [_Any!]!) {
                          _entities(representations: $r) { ... on Cliente { id nome rendaMensal } }
                        }
                        """)
                .variable("r", List.of(Map.of("__typename", "Cliente", "id", "cli-007")))
                .execute()
                .path("_entities[0].id").entity(String.class).isEqualTo("cli-007")
                .path("_entities[0].rendaMensal").hasValue();
    }

    @Test
    @WithMockUser(username = "cli-001")
    void sdlDoSubgraphExpoeDiretivasDeFederacao() {
        tester.document("{ _service { sdl } }")
                .execute()
                .path("_service.sdl").entity(String.class)
                .satisfies(sdl -> assertThat(sdl)
                        .contains("type Cliente @key(fields : \"id\"")
                        .contains("scoreCredito: Int! @inaccessible"));
    }
}
