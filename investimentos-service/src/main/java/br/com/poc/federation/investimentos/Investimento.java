package br.com.poc.federation.investimentos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record Investimento(
        String id,
        String clienteId,
        String produtoCodigo,
        Tipo tipo,
        BigDecimal valorAplicado,
        BigDecimal percentualIndexador,
        LocalDate dataAplicacao,
        LocalDate dataVencimento,
        boolean liquidezDiaria) {

    public static final BigDecimal CDI_ANUAL = new BigDecimal("14.65");

    public enum Tipo { CDB, LCI, TESOURO, FUNDO }

    public long diasAplicado() {
        return ChronoUnit.DAYS.between(dataAplicacao, LocalDate.now());
    }

    public BigDecimal valorEm(LocalDate data) {
        double taxaAnual = CDI_ANUAL.doubleValue() * percentualIndexador.doubleValue() / 10_000;
        double anos = ChronoUnit.DAYS.between(dataAplicacao, data) / 365.0;
        return valorAplicado.multiply(BigDecimal.valueOf(Math.pow(1 + taxaAnual, anos))).setScale(2, RoundingMode.HALF_EVEN);
    }

    public BigDecimal valorAtual() {
        return valorEm(LocalDate.now());
    }

    public BigDecimal rendimentoBruto() {
        return valorAtual().subtract(valorAplicado);
    }

    public BigDecimal rentabilidadePercentual() {
        return rendimentoBruto().multiply(BigDecimal.valueOf(100)).divide(valorAplicado, 2, RoundingMode.HALF_EVEN);
    }

    public BigDecimal aliquotaIR() {
        if (tipo == Tipo.LCI) {
            return BigDecimal.ZERO;
        }
        long dias = diasAplicado();
        String aliquota = dias <= 180 ? "22.5" : dias <= 360 ? "20.0" : dias <= 720 ? "17.5" : "15.0";
        return new BigDecimal(aliquota);
    }

    public BigDecimal impostoEstimado() {
        return rendimentoBruto().multiply(aliquotaIR()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN);
    }

    public BigDecimal valorLiquido() {
        return valorAtual().subtract(impostoEstimado());
    }
}
