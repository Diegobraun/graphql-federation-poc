package br.com.poc.federation.produtos;

import java.math.BigDecimal;
import java.util.List;

public record Produto(
        String codigo,
        String nome,
        Categoria categoria,
        String descricao,
        Indexador indexador,
        BigDecimal taxaReferencia,
        Integer prazoMinimoMeses,
        Integer prazoMaximoMeses,
        BigDecimal valorMinimo,
        boolean ativo,
        List<Tarifa> tarifas,
        String regulamento,
        List<String> documentosLegais) {

    public enum Categoria { CONTA, EMPRESTIMO, INVESTIMENTO }

    public enum Indexador { PRE, CDI, IPCA, SELIC }

    public enum Periodicidade { UNICA, MENSAL, ANUAL }

    public record Tarifa(String nome, BigDecimal valor, Periodicidade periodicidade) {
    }
}
