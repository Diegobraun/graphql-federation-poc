package br.com.poc.federation.emprestimos;

import br.com.poc.federation.common.dados.CodigosProduto;
import br.com.poc.federation.emprestimos.Referencias.Produto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public final class Credito {

    private static final int PRAZO_MAXIMO = 48;

    private Credito() {
    }

    public record OfertaCredito(BigDecimal limitePreAprovado, BigDecimal taxaJurosMensal, int prazoMaximoMeses, String motivo, Produto produto) {
    }

    public record SimulacaoInput(String produtoCodigo, BigDecimal valor, int quantidadeParcelas) {
    }

    public record Simulacao(Produto produto, BigDecimal valorSolicitado, int quantidadeParcelas, BigDecimal taxaJurosMensal,
                            BigDecimal valorParcela, BigDecimal totalPago, BigDecimal totalJuros, BigDecimal cetAnual, List<Parcela> parcelas) {
    }

    public static OfertaCredito oferta(BigDecimal renda, int score, BigDecimal saldoDevedorAtual) {
        record Faixa(int scoreMinimo, int multiplicador, String taxa, String motivo) {
        }
        Faixa faixa = List.of(
                        new Faixa(800, 8, "1.49", "Score excelente"),
                        new Faixa(650, 5, "2.29", "Score bom"),
                        new Faixa(500, 3, "3.19", "Score regular"),
                        new Faixa(0, 0, "0", "Score abaixo da política de crédito"))
                .stream().filter(f -> score >= f.scoreMinimo()).findFirst().orElseThrow();
        BigDecimal limite = renda.multiply(BigDecimal.valueOf(faixa.multiplicador())).subtract(saldoDevedorAtual).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_EVEN);
        String motivo = limite.signum() == 0 && faixa.multiplicador() > 0 ? "Limite consumido por contratos em aberto" : faixa.motivo();
        return new OfertaCredito(limite, new BigDecimal(faixa.taxa()), faixa.multiplicador() == 0 ? 0 : PRAZO_MAXIMO, motivo,
                new Produto(CodigosProduto.EMPRESTIMO_PESSOAL));
    }

    public static Simulacao simular(SimulacaoInput input) {
        if (input.valor().signum() <= 0 || input.quantidadeParcelas() < 1 || input.quantidadeParcelas() > 120) {
            throw new IllegalArgumentException("Valor deve ser positivo e parcelas entre 1 e 120");
        }
        BigDecimal taxa = Taxas.mensal(input.produtoCodigo());
        List<Parcela> parcelas = TabelaPrice.gerar(input.valor(), taxa, input.quantidadeParcelas(),
                LocalDate.now().plusMonths(1), n -> Parcela.Status.EM_ABERTO);
        BigDecimal totalPago = parcelas.stream().map(Parcela::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cetAnual = BigDecimal.ONE.add(taxa.add(new BigDecimal("0.12")).movePointLeft(2)).pow(12)
                .subtract(BigDecimal.ONE).movePointRight(2).setScale(2, RoundingMode.HALF_EVEN);
        return new Simulacao(new Produto(input.produtoCodigo()), input.valor(), input.quantidadeParcelas(), taxa,
                parcelas.getFirst().valor(), totalPago, totalPago.subtract(input.valor()), cetAnual, parcelas);
    }
}
