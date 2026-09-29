package br.com.poc.federation.contas;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Lancamento(String id, String contaId, OffsetDateTime dataHora, String descricao, BigDecimal valor, Tipo tipo, Categoria categoria) {

    public enum Tipo { CREDITO, DEBITO }

    public enum Categoria { SALARIO, PIX_RECEBIDO, PIX_ENVIADO, COMPRA_DEBITO, BOLETO, TARIFA, RENDIMENTO, PARCELA_EMPRESTIMO }
}
