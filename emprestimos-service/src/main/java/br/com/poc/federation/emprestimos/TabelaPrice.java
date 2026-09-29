package br.com.poc.federation.emprestimos;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public final class TabelaPrice {

    private TabelaPrice() {
    }

    public static BigDecimal valorParcela(BigDecimal principal, BigDecimal taxaMensalPercentual, int parcelas) {
        BigDecimal i = taxaMensalPercentual.divide(BigDecimal.valueOf(100), MathContext.DECIMAL64);
        BigDecimal fator = BigDecimal.ONE.add(i).pow(parcelas, MathContext.DECIMAL64);
        return principal.multiply(i.multiply(fator)).divide(fator.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_EVEN);
    }

    public static List<Parcela> gerar(BigDecimal principal, BigDecimal taxaMensalPercentual, int quantidade, LocalDate primeiroVencimento,
                                      IntFunction<Parcela.Status> statusDaParcela) {
        BigDecimal i = taxaMensalPercentual.divide(BigDecimal.valueOf(100), MathContext.DECIMAL64);
        BigDecimal pmt = valorParcela(principal, taxaMensalPercentual, quantidade);
        BigDecimal saldo = principal;
        List<Parcela> parcelas = new ArrayList<>(quantidade);
        for (int n = 1; n <= quantidade; n++) {
            BigDecimal juros = saldo.multiply(i).setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal amortizacao = n == quantidade ? saldo : pmt.subtract(juros);
            saldo = saldo.subtract(amortizacao);
            LocalDate vencimento = primeiroVencimento.plusMonths(n - 1L);
            Parcela.Status status = statusDaParcela.apply(n);
            parcelas.add(new Parcela(n, vencimento, pmt, amortizacao, juros, status, status == Parcela.Status.PAGA ? vencimento : null));
        }
        return List.copyOf(parcelas);
    }
}
