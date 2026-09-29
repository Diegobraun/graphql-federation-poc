package br.com.poc.federation.clientes;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ClienteHttpTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void graphqlSemTokenRetorna401() throws Exception {
        mvc.perform(post("/graphql").contentType(MediaType.APPLICATION_JSON).content("{\"query\":\"{ me { id } }\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void restRetornaPayloadCompletoParaDono() throws Exception {
        mvc.perform(get("/api/v1/clientes/cli-002").with(jwt().jwt(j -> j.subject("cli-002"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.id").value("cli-002"))
                .andExpect(jsonPath("$.historicoAlteracoes.length()").value(15));
    }

    @Test
    void restNegaAcessoAOutroCliente() throws Exception {
        mvc.perform(get("/api/v1/clientes/cli-002").with(jwt().jwt(j -> j.subject("cli-001"))))
                .andExpect(status().isForbidden());
    }
}
