package br.com.poc.federation.investimentos;

import br.com.poc.federation.common.erro.NaoEncontradoException;
import br.com.poc.federation.common.seguranca.Acesso;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InvestimentoRestController {

    private final InvestimentoRepository repository;
    private final Acesso acesso;

    public InvestimentoRestController(InvestimentoRepository repository, Acesso acesso) {
        this.repository = repository;
        this.acesso = acesso;
    }

    @GetMapping("/api/v1/clientes/{clienteId}/investimentos")
    public List<InvestimentoCompleto> doCliente(@PathVariable String clienteId) {
        acesso.exigirAcessoAoCliente(clienteId);
        return repository.doCliente(clienteId).stream().map(InvestimentoRestController::completo).toList();
    }

    @GetMapping("/api/v1/investimentos/{id}")
    public InvestimentoCompleto buscar(@PathVariable String id) {
        Investimento investimento = repository.buscar(id).orElseThrow(() -> new NaoEncontradoException("Investimento", id));
        acesso.exigirAcessoAoCliente(investimento.clienteId());
        return completo(investimento);
    }

    private static InvestimentoCompleto completo(Investimento i) {
        List<PosicaoMensal> historico = new ArrayList<>();
        LocalDate mes = i.dataAplicacao().withDayOfMonth(1).plusMonths(1);
        BigDecimal anterior = i.valorAplicado();
        while (!mes.isAfter(LocalDate.now())) {
            BigDecimal valor = i.valorEm(mes);
            BigDecimal rendimentoMes = valor.subtract(anterior).multiply(BigDecimal.valueOf(100)).divide(anterior, 4, RoundingMode.HALF_EVEN);
            historico.add(new PosicaoMensal(mes, valor, rendimentoMes, i.percentualIndexador()));
            anterior = valor;
            mes = mes.plusMonths(1);
        }
        return new InvestimentoCompleto(i, i.valorAtual(), i.rendimentoBruto(), i.rentabilidadePercentual(), i.aliquotaIR(),
                i.impostoEstimado(), i.valorLiquido(), historico, "B3 S.A. - Brasil, Bolsa, Balcão", "Emissor Exemplo S.A.", "AAA(bra)");
    }

    public record InvestimentoCompleto(Investimento investimento, BigDecimal valorAtual, BigDecimal rendimentoBruto,
                                       BigDecimal rentabilidadePercentual, BigDecimal aliquotaIR, BigDecimal impostoEstimado,
                                       BigDecimal valorLiquido, List<PosicaoMensal> historicoRentabilidade, String custodiante,
                                       String emissor, String ratingEmissor) {
    }

    public record PosicaoMensal(LocalDate referencia, BigDecimal valor, BigDecimal rentabilidadeMesPercentual, BigDecimal percentualIndexador) {
    }
}
