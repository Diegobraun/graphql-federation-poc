package br.com.poc.federation.emprestimos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record Parcela(int numero, LocalDate vencimento, BigDecimal valor, BigDecimal amortizacao, BigDecimal juros,
                      Status status, LocalDate dataPagamento) {

    public enum Status { PAGA, EM_ABERTO, ATRASADA }

    public int diasEmAtraso() {
        return status == Status.ATRASADA ? (int) ChronoUnit.DAYS.between(vencimento, LocalDate.now()) : 0;
    }
}
