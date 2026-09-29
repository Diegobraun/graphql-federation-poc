package br.com.poc.federation.emprestimos;

import br.com.poc.federation.common.seguranca.Acesso;
import br.com.poc.federation.emprestimos.Credito.OfertaCredito;
import br.com.poc.federation.emprestimos.Credito.Simulacao;
import br.com.poc.federation.emprestimos.Credito.SimulacaoInput;
import br.com.poc.federation.emprestimos.Referencias.Cliente;
import br.com.poc.federation.emprestimos.Referencias.Conta;
import br.com.poc.federation.emprestimos.Referencias.Produto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class EmprestimoGraphQlController {

    private final EmprestimoRepository repository;
    private final Acesso acesso;

    public EmprestimoGraphQlController(EmprestimoRepository repository, Acesso acesso) {
        this.repository = repository;
        this.acesso = acesso;
    }

    @QueryMapping
    public Emprestimo emprestimo(@Argument String id) {
        return repository.buscar(id).map(this::autorizado).orElse(null);
    }

    @QueryMapping
    public Simulacao simularEmprestimo(@Argument SimulacaoInput input) {
        return Credito.simular(input);
    }

    @EntityMapping("Emprestimo")
    public Emprestimo referenciaEmprestimo(@Argument String id) {
        return emprestimo(id);
    }

    @EntityMapping("Cliente")
    public Cliente referenciaCliente(@Argument String id, Map<String, Object> representation) {
        acesso.exigirAcessoAoCliente(id);
        Object renda = representation.get("rendaMensal");
        Object score = representation.get("scoreCredito");
        return new Cliente(id,
                renda == null ? null : new BigDecimal(renda.toString()),
                score == null ? null : Integer.valueOf(score.toString()));
    }

    @EntityMapping("Produto")
    public Produto referenciaProduto(@Argument String codigo) {
        return new Produto(codigo);
    }

    @SchemaMapping(typeName = "Cliente")
    public List<Emprestimo> emprestimos(Cliente cliente, @Argument Emprestimo.Status status) {
        return repository.doCliente(cliente.id()).stream()
                .filter(e -> status == null || e.status() == status)
                .toList();
    }

    @SchemaMapping(typeName = "Cliente")
    public OfertaCredito ofertaCredito(Cliente cliente) {
        return Credito.oferta(cliente.rendaMensal(), cliente.scoreCredito(), saldoDevedorEmAberto(cliente.id()));
    }

    @SchemaMapping(typeName = "Cliente")
    public BigDecimal comprometimentoRenda(Cliente cliente) {
        BigDecimal parcelas = repository.doCliente(cliente.id()).stream()
                .filter(Emprestimo::emAberto)
                .map(Emprestimo::valorParcela)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return parcelas.multiply(BigDecimal.valueOf(100)).divide(cliente.rendaMensal(), 2, RoundingMode.HALF_EVEN);
    }

    @SchemaMapping(typeName = "Produto")
    @PreAuthorize("hasAuthority('SCOPE_backoffice')")
    public long contratosAtivos(Produto produto) {
        return repository.contratosAtivos(produto.codigo());
    }

    @SchemaMapping
    public Parcela proximaParcela(Emprestimo emprestimo) {
        return emprestimo.proximaParcela().orElse(null);
    }

    @SchemaMapping
    public List<Parcela> parcelas(Emprestimo emprestimo, @Argument Parcela.Status status) {
        return emprestimo.parcelas().stream().filter(p -> status == null || p.status() == status).toList();
    }

    @SchemaMapping
    public Cliente cliente(Emprestimo emprestimo) {
        return new Cliente(emprestimo.clienteId());
    }

    @SchemaMapping
    public Conta contaDebito(Emprestimo emprestimo) {
        return new Conta(emprestimo.contaDebitoId());
    }

    @SchemaMapping
    public Produto produto(Emprestimo emprestimo) {
        return new Produto(emprestimo.produtoCodigo());
    }

    private BigDecimal saldoDevedorEmAberto(String clienteId) {
        return repository.doCliente(clienteId).stream()
                .filter(Emprestimo::emAberto)
                .map(Emprestimo::saldoDevedor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Emprestimo autorizado(Emprestimo emprestimo) {
        acesso.exigirAcessoAoCliente(emprestimo.clienteId());
        return emprestimo;
    }
}
