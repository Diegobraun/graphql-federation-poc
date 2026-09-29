package br.com.poc.federation.investimentos;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class InvestimentoTest {

    @Test
    void lciEhIsentaDeImposto() {
        var lci = investimento(Investimento.Tipo.LCI, LocalDate.now().minusDays(400));
        assertThat(lci.aliquotaIR()).isZero();
        assertThat(lci.valorLiquido()).isEqualByComparingTo(lci.valorAtual());
    }

    @Test
    void cdbSegueTabelaRegressivaDeIR() {
        assertThat(investimento(Investimento.Tipo.CDB, LocalDate.now().minusDays(100)).aliquotaIR()).isEqualByComparingTo("22.5");
        assertThat(investimento(Investimento.Tipo.CDB, LocalDate.now().minusDays(300)).aliquotaIR()).isEqualByComparingTo("20.0");
        assertThat(investimento(Investimento.Tipo.CDB, LocalDate.now().minusDays(500)).aliquotaIR()).isEqualByComparingTo("17.5");
        assertThat(investimento(Investimento.Tipo.CDB, LocalDate.now().minusDays(900)).aliquotaIR()).isEqualByComparingTo("15.0");
    }

    @Test
    void cemPorCentoDoCdiRendeOCdiEmUmAno() {
        var cdb = investimento(Investimento.Tipo.CDB, LocalDate.now().minusDays(365));
        assertThat(cdb.rentabilidadePercentual()).isEqualByComparingTo(Investimento.CDI_ANUAL);
    }

    private static Investimento investimento(Investimento.Tipo tipo, LocalDate aplicacao) {
        return new Investimento("inv-teste", "cli-001", "X", tipo, new BigDecimal("10000.00"), new BigDecimal("100"), aplicacao, null, true);
    }
}
