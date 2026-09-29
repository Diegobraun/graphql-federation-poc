package br.com.poc.federation.investimentos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record Carteira(BigDecimal totalAplicado, BigDecimal patrimonioBruto, BigDecimal rendimentoBruto, BigDecimal patrimonioLiquido,
                       BigDecimal rentabilidadePercentual, int quantidadeAtivos, List<Alocacao> alocacao) {

    public record Alocacao(Investimento.Tipo tipo, BigDecimal valor, BigDecimal percentual) {
    }

    public static Carteira de(List<Investimento> investimentos) {
        BigDecimal aplicado = somar(investimentos, Investimento::valorAplicado);
        BigDecimal bruto = somar(investimentos, Investimento::valorAtual);
        BigDecimal liquido = somar(investimentos, Investimento::valorLiquido);
        BigDecimal rentabilidade = aplicado.signum() == 0 ? BigDecimal.ZERO
                : bruto.subtract(aplicado).multiply(BigDecimal.valueOf(100)).divide(aplicado, 2, RoundingMode.HALF_EVEN);
        Map<Investimento.Tipo, BigDecimal> porTipo = investimentos.stream()
                .collect(Collectors.groupingBy(Investimento::tipo, Collectors.reducing(BigDecimal.ZERO, Investimento::valorAtual, BigDecimal::add)));
        List<Alocacao> alocacao = porTipo.entrySet().stream()
                .map(e -> new Alocacao(e.getKey(), e.getValue(),
                        e.getValue().multiply(BigDecimal.valueOf(100)).divide(bruto, 2, RoundingMode.HALF_EVEN)))
                .sorted(Comparator.comparing(Alocacao::valor).reversed())
                .toList();
        return new Carteira(aplicado, bruto, bruto.subtract(aplicado), liquido, rentabilidade, investimentos.size(), alocacao);
    }

    private static BigDecimal somar(List<Investimento> investimentos, Function<Investimento, BigDecimal> campo) {
        return investimentos.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
