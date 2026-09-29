package br.com.poc.federation.emprestimos;

import br.com.poc.federation.common.dados.CodigosProduto;
import br.com.poc.federation.common.dados.Seed;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.springframework.stereotype.Repository;

@Repository
public class EmprestimoRepository {

    private final Map<String, Emprestimo> emprestimos = new LinkedHashMap<>();

    public EmprestimoRepository() {
        LocalDate hoje = LocalDate.now();
        for (String clienteId : Seed.clienteIds()) {
            Random random = Seed.random("emprestimo", clienteId);
            int quantidade = List.of(0, 0, 1, 1, 1, 2, 3).get(random.nextInt(7));
            for (int e = 1; e <= quantidade; e++) {
                String id = "emp-" + Seed.sufixo(clienteId) + "-" + e;
                String produto = Seed.escolher(random, CodigosProduto.EMPRESTIMOS);
                int parcelas = Seed.escolher(random, List.of(12, 24, 36, 48));
                int mesesDesdeContratacao = 1 + random.nextInt(parcelas + 6);
                LocalDate contratacao = hoje.minusMonths(mesesDesdeContratacao).minusDays(random.nextInt(20));
                boolean inadimplente = random.nextInt(8) == 0;
                BigDecimal valor = Seed.valor(random, 1_000, CodigosProduto.EMPRESTIMO_VEICULO.equals(produto) ? 45_000 : 15_000);
                BigDecimal taxa = Taxas.mensal(produto);
                LocalDate primeiroVencimento = contratacao.plusMonths(1);
                List<Parcela> lista = TabelaPrice.gerar(valor, taxa, parcelas, primeiroVencimento, n -> {
                    LocalDate vencimento = primeiroVencimento.plusMonths(n - 1L);
                    if (!vencimento.isBefore(hoje)) {
                        return Parcela.Status.EM_ABERTO;
                    }
                    boolean ultimasVencidas = !vencimento.isBefore(hoje.minusMonths(2));
                    return inadimplente && ultimasVencidas ? Parcela.Status.ATRASADA : Parcela.Status.PAGA;
                });
                emprestimos.put(id, new Emprestimo(id, clienteId, "cta-" + Seed.sufixo(clienteId) + "-1", produto,
                        "CTR-%s-%06d".formatted(contratacao.getYear(), random.nextInt(999_999)), valor, taxa, contratacao, lista));
            }
        }
    }

    public Optional<Emprestimo> buscar(String id) {
        return Optional.ofNullable(emprestimos.get(id));
    }

    public List<Emprestimo> doCliente(String clienteId) {
        return emprestimos.values().stream().filter(e -> e.clienteId().equals(clienteId)).toList();
    }

    public long contratosAtivos(String produtoCodigo) {
        return emprestimos.values().stream().filter(e -> e.produtoCodigo().equals(produtoCodigo) && e.emAberto()).count();
    }
}
