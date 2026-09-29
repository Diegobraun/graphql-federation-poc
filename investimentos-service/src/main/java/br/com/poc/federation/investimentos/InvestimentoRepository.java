package br.com.poc.federation.investimentos;

import static br.com.poc.federation.common.dados.CodigosProduto.*;

import br.com.poc.federation.common.dados.Seed;
import br.com.poc.federation.investimentos.Investimento.Tipo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.springframework.stereotype.Repository;

@Repository
public class InvestimentoRepository {

    private record Condicao(Tipo tipo, String percentual, Integer prazoMeses, boolean liquidezDiaria) {
    }

    private static final Map<String, Condicao> CONDICOES = Map.of(
            CDB_LIQUIDEZ, new Condicao(Tipo.CDB, "100", null, true),
            CDB_2_ANOS, new Condicao(Tipo.CDB, "112", 24, false),
            LCI_90, new Condicao(Tipo.LCI, "93", 24, false),
            TESOURO_SELIC, new Condicao(Tipo.TESOURO, "100.7", 72, true),
            FUNDO_MULTIMERCADO, new Condicao(Tipo.FUNDO, "118", null, true));

    private final Map<String, Investimento> investimentos = new LinkedHashMap<>();

    public InvestimentoRepository() {
        LocalDate hoje = LocalDate.now();
        for (String clienteId : Seed.clienteIds()) {
            Random random = Seed.random("investimento", clienteId);
            int quantidade = random.nextInt(6);
            for (int i = 1; i <= quantidade; i++) {
                String produto = Seed.escolher(random, INVESTIMENTOS);
                Condicao condicao = CONDICOES.get(produto);
                LocalDate aplicacao = hoje.minusDays(30 + random.nextInt(900));
                String id = "inv-" + Seed.sufixo(clienteId) + "-" + i;
                investimentos.put(id, new Investimento(id, clienteId, produto, condicao.tipo(), Seed.valor(random, 1_000, 150_000),
                        new BigDecimal(condicao.percentual()), aplicacao,
                        condicao.prazoMeses() == null ? null : aplicacao.plusMonths(condicao.prazoMeses()), condicao.liquidezDiaria()));
            }
        }
    }

    public Optional<Investimento> buscar(String id) {
        return Optional.ofNullable(investimentos.get(id));
    }

    public List<Investimento> doCliente(String clienteId) {
        return investimentos.values().stream().filter(i -> i.clienteId().equals(clienteId)).toList();
    }

    public BigDecimal volumeAplicado(String produtoCodigo) {
        return investimentos.values().stream()
                .filter(i -> i.produtoCodigo().equals(produtoCodigo))
                .map(Investimento::valorAtual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
