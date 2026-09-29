package br.com.poc.federation.investimentos;

import br.com.poc.federation.common.seguranca.Acesso;
import br.com.poc.federation.investimentos.Referencias.Cliente;
import br.com.poc.federation.investimentos.Referencias.Produto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class InvestimentoGraphQlController {

    private final InvestimentoRepository repository;
    private final Acesso acesso;

    public InvestimentoGraphQlController(InvestimentoRepository repository, Acesso acesso) {
        this.repository = repository;
        this.acesso = acesso;
    }

    @QueryMapping
    public Investimento investimento(@Argument String id) {
        return repository.buscar(id).map(this::autorizado).orElse(null);
    }

    @EntityMapping("Investimento")
    public Investimento referenciaInvestimento(@Argument String id) {
        return investimento(id);
    }

    @EntityMapping("Cliente")
    public Cliente referenciaCliente(@Argument String id) {
        acesso.exigirAcessoAoCliente(id);
        return new Cliente(id);
    }

    @EntityMapping("Produto")
    public Produto referenciaProduto(@Argument String codigo) {
        return new Produto(codigo);
    }

    @SchemaMapping(typeName = "Cliente")
    public List<Investimento> investimentos(Cliente cliente, @Argument Investimento.Tipo tipo) {
        return repository.doCliente(cliente.id()).stream().filter(i -> tipo == null || i.tipo() == tipo).toList();
    }

    @BatchMapping(typeName = "Cliente")
    public Map<Cliente, Carteira> carteira(List<Cliente> clientes) {
        return clientes.stream().collect(Collectors.toMap(Function.identity(), c -> Carteira.de(repository.doCliente(c.id()))));
    }

    @SchemaMapping(typeName = "Produto")
    @PreAuthorize("hasAuthority('SCOPE_backoffice')")
    public BigDecimal volumeAplicado(Produto produto) {
        return repository.volumeAplicado(produto.codigo());
    }

    @SchemaMapping
    public Produto produto(Investimento investimento) {
        return new Produto(investimento.produtoCodigo());
    }

    @SchemaMapping
    public Cliente cliente(Investimento investimento) {
        return new Cliente(investimento.clienteId());
    }

    private Investimento autorizado(Investimento investimento) {
        acesso.exigirAcessoAoCliente(investimento.clienteId());
        return investimento;
    }
}
