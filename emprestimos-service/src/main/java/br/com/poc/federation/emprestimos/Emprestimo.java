package br.com.poc.federation.emprestimos;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record Emprestimo(
        String id,
        String clienteId,
        String contaDebitoId,
        String produtoCodigo,
        String contrato,
        BigDecimal valorContratado,
        BigDecimal taxaJurosMensal,
        LocalDate dataContratacao,
        List<Parcela> parcelas) {

    public enum Status { ATIVO, QUITADO, INADIMPLENTE }

    public Status status() {
        if (parcelas.stream().anyMatch(p -> p.status() == Parcela.Status.ATRASADA)) {
            return Status.INADIMPLENTE;
        }
        return parcelas.stream().allMatch(p -> p.status() == Parcela.Status.PAGA) ? Status.QUITADO : Status.ATIVO;
    }

    public int quantidadeParcelas() {
        return parcelas.size();
    }

    public BigDecimal valorParcela() {
        return parcelas.getFirst().valor();
    }

    public BigDecimal taxaJurosAnual() {
        BigDecimal mensal = taxaJurosMensal.divide(BigDecimal.valueOf(100), MathContext.DECIMAL64);
        return BigDecimal.ONE.add(mensal).pow(12).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_EVEN);
    }

    public BigDecimal saldoDevedor() {
        return parcelas.stream()
                .filter(p -> p.status() != Parcela.Status.PAGA)
                .map(Parcela::amortizacao)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int parcelasPagas() {
        return (int) parcelas.stream().filter(p -> p.status() == Parcela.Status.PAGA).count();
    }

    public Optional<Parcela> proximaParcela() {
        return parcelas.stream().filter(p -> p.status() != Parcela.Status.PAGA).findFirst();
    }

    public boolean emAberto() {
        return status() != Status.QUITADO;
    }
}
