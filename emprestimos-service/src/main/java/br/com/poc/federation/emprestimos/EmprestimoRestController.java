package br.com.poc.federation.emprestimos;

import br.com.poc.federation.common.erro.NaoEncontradoException;
import br.com.poc.federation.common.seguranca.Acesso;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmprestimoRestController {

    private final EmprestimoRepository repository;
    private final Acesso acesso;

    public EmprestimoRestController(EmprestimoRepository repository, Acesso acesso) {
        this.repository = repository;
        this.acesso = acesso;
    }

    @GetMapping("/api/v1/clientes/{clienteId}/emprestimos")
    public List<EmprestimoCompleto> doCliente(@PathVariable String clienteId) {
        acesso.exigirAcessoAoCliente(clienteId);
        return repository.doCliente(clienteId).stream().map(EmprestimoRestController::completo).toList();
    }

    @GetMapping("/api/v1/emprestimos/{id}")
    public EmprestimoCompleto buscar(@PathVariable String id) {
        Emprestimo emprestimo = repository.buscar(id).orElseThrow(() -> new NaoEncontradoException("Empréstimo", id));
        acesso.exigirAcessoAoCliente(emprestimo.clienteId());
        return completo(emprestimo);
    }

    private static EmprestimoCompleto completo(Emprestimo e) {
        var seguro = new Seguro("Seguro Prestamista", "Seguradora Exemplo", e.valorContratado().multiply(new BigDecimal("0.035")), true);
        var documentos = IntStream.range(0, 5)
                .mapToObj(i -> new Documento(List.of("CCB", "Termo de seguro", "Autorização de débito", "Tabela CET", "Proposta").get(i),
                        "https://documentos.exemplo.com/" + e.contrato() + "/" + i + ".pdf", e.dataContratacao()))
                .toList();
        var custos = new ComposicaoCet(e.valorContratado().multiply(new BigDecimal("0.0038")), seguro.valor(), new BigDecimal("0"), e.taxaJurosAnual());
        return new EmprestimoCompleto(e, e.status(), e.saldoDevedor(), e.valorParcela(), e.taxaJurosAnual(), e.parcelasPagas(),
                e.proximaParcela().orElse(null), seguro, documentos, custos, List.of());
    }

    public record EmprestimoCompleto(Emprestimo emprestimo, Emprestimo.Status status, BigDecimal saldoDevedor, BigDecimal valorParcela,
                                     BigDecimal taxaJurosAnual, int parcelasPagas, Parcela proximaParcela, Seguro seguro,
                                     List<Documento> documentos, ComposicaoCet composicaoCet, List<String> renegociacoes) {
    }

    public record Seguro(String nome, String seguradora, BigDecimal valor, boolean ativo) {
    }

    public record Documento(String nome, String url, LocalDate emitidoEm) {
    }

    public record ComposicaoCet(BigDecimal iof, BigDecimal seguro, BigDecimal tarifaCadastro, BigDecimal jurosAnual) {
    }
}
