package br.com.poc.federation.contas;

import br.com.poc.federation.common.paginacao.Conexao;
import br.com.poc.federation.common.seguranca.Acesso;
import br.com.poc.federation.contas.Referencias.Cliente;
import br.com.poc.federation.contas.Referencias.Produto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ContaGraphQlController {

    private static final int LIMITE_EXTRATO = 100;

    private final ContaRepository repository;
    private final Acesso acesso;

    public ContaGraphQlController(ContaRepository repository, Acesso acesso) {
        this.repository = repository;
        this.acesso = acesso;
    }

    @QueryMapping
    public Conta conta(@Argument String id) {
        return repository.buscar(id).map(this::autorizada).orElse(null);
    }

    @EntityMapping("Conta")
    public Conta referenciaConta(@Argument String id) {
        return conta(id);
    }

    @EntityMapping("Cliente")
    public Cliente referenciaCliente(@Argument String id) {
        acesso.exigirAcessoAoCliente(id);
        return new Cliente(id);
    }

    @SchemaMapping(typeName = "Cliente")
    public List<Conta> contas(Cliente cliente, @Argument Conta.Status status) {
        return repository.doCliente(cliente.id()).stream()
                .filter(c -> status == null || c.status() == status)
                .toList();
    }

    @BatchMapping(typeName = "Cliente")
    public Map<Cliente, BigDecimal> saldoTotalEmContas(List<Cliente> clientes) {
        return clientes.stream().collect(Collectors.toMap(Function.identity(), c -> repository.doCliente(c.id()).stream()
                .map(Conta::saldoDisponivel)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    @SchemaMapping
    public Cliente titular(Conta conta) {
        return new Cliente(conta.clienteId());
    }

    @SchemaMapping
    public Produto produto(Conta conta) {
        return new Produto(conta.produtoCodigo());
    }

    @SchemaMapping
    public Conexao<Lancamento> extrato(Conta conta, @Argument LocalDate inicio, @Argument LocalDate fim,
                                       @Argument Lancamento.Tipo tipo, @Argument Integer first, @Argument String after) {
        List<Lancamento> filtrados = repository.lancamentos(conta.id()).stream()
                .filter(l -> inicio == null || !l.dataHora().toLocalDate().isBefore(inicio))
                .filter(l -> fim == null || !l.dataHora().toLocalDate().isAfter(fim))
                .filter(l -> tipo == null || l.tipo() == tipo)
                .toList();
        return Conexao.paginar(filtrados, first, after, Lancamento::id, LIMITE_EXTRATO);
    }

    private Conta autorizada(Conta conta) {
        acesso.exigirAcessoAoCliente(conta.clienteId());
        return conta;
    }
}
