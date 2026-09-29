package br.com.poc.federation.emprestimos;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TabelaPriceTest {

    @Test
    void parcelasConstantesQuitamOPrincipal() {
        var parcelas = TabelaPrice.gerar(new BigDecimal("10000"), new BigDecimal("2"), 12, LocalDate.of(2026, 1, 10), n -> Parcela.Status.EM_ABERTO);

        assertThat(parcelas).hasSize(12);
        assertThat(parcelas).extracting(Parcela::valor).containsOnly(new BigDecimal("945.60"));
        assertThat(parcelas.stream().map(Parcela::amortizacao).reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo("10000");
        assertThat(parcelas.getFirst().juros()).isEqualByComparingTo("200.00");
        assertThat(parcelas.getLast().vencimento()).isEqualTo(LocalDate.of(2026, 12, 10));
    }
}
