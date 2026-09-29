package br.com.poc.federation.emprestimos;

import static br.com.poc.federation.common.dados.CodigosProduto.*;

import java.math.BigDecimal;
import java.util.Map;

public final class Taxas {

    private static final Map<String, BigDecimal> TAXA_MENSAL = Map.of(
            EMPRESTIMO_PESSOAL, new BigDecimal("3.49"),
            EMPRESTIMO_CONSIGNADO, new BigDecimal("1.79"),
            EMPRESTIMO_VEICULO, new BigDecimal("1.99"));

    private Taxas() {
    }

    public static BigDecimal mensal(String produtoCodigo) {
        BigDecimal taxa = TAXA_MENSAL.get(produtoCodigo);
        if (taxa == null) {
            throw new IllegalArgumentException("Produto não é de empréstimo: " + produtoCodigo);
        }
        return taxa;
    }
}
