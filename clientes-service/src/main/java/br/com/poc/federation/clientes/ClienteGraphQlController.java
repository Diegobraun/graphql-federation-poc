package br.com.poc.federation.clientes;

import br.com.poc.federation.common.paginacao.Conexao;
import br.com.poc.federation.common.seguranca.Acesso;
import java.time.LocalDate;
import java.time.Period;
import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class ClienteGraphQlController {

    private static final int LIMITE_PAGINA = 50;

    private final ClienteRepository repository;
    private final Acesso acesso;

    public ClienteGraphQlController(ClienteRepository repository, Acesso acesso) {
        this.repository = repository;
        this.acesso = acesso;
    }

    @QueryMapping
    public Cliente me() {
        return repository.buscar(acesso.clienteLogado()).orElse(null);
    }

    @QueryMapping
    public Cliente cliente(@Argument String id) {
        acesso.exigirAcessoAoCliente(id);
        return repository.buscar(id).orElse(null);
    }

    @QueryMapping
    @PreAuthorize("hasAuthority('SCOPE_backoffice')")
    public Conexao<Cliente> clientes(@Argument FiltroClientes filtro, @Argument Integer first, @Argument String after) {
        return Conexao.paginar(repository.listar(filtro), first, after, Cliente::id, LIMITE_PAGINA);
    }

    @EntityMapping("Cliente")
    public Cliente referenciaCliente(@Argument String id) {
        acesso.exigirAcessoAoCliente(id);
        return repository.buscar(id).orElse(null);
    }

    @SchemaMapping
    public int idade(Cliente cliente) {
        return Period.between(cliente.dataNascimento(), LocalDate.now()).getYears();
    }
}
