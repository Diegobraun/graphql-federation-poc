package br.com.poc.federation.contas;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Conta(
        String id,
        String clienteId,
        String agencia,
        String numero,
        Tipo tipo,
        Status status,
        BigDecimal saldoDisponivel,
        BigDecimal saldoBloqueado,
        BigDecimal limiteChequeEspecial,
        LocalDate dataAbertura,
        String produtoCodigo) {

    public enum Tipo { CORRENTE, POUPANCA }

    public enum Status { ATIVA, BLOQUEADA, ENCERRADA }

    public Saldo saldo() {
        BigDecimal total = saldoDisponivel.add(saldoBloqueado);
        return new Saldo(saldoDisponivel, saldoBloqueado, total, saldoDisponivel.add(limiteChequeEspecial));
    }

    public record Saldo(BigDecimal disponivel, BigDecimal bloqueado, BigDecimal total, BigDecimal disponivelComLimite) {
    }
}
