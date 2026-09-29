package br.com.poc.federation.contas;

import br.com.poc.federation.common.dados.CodigosProduto;
import br.com.poc.federation.common.dados.Seed;
import br.com.poc.federation.contas.Lancamento.Categoria;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.springframework.stereotype.Repository;

@Repository
public class ContaRepository {

    private static final int LANCAMENTOS_CORRENTE = 150;
    private static final int LANCAMENTOS_POUPANCA = 20;
    private static final int DIAS_HISTORICO = 180;

    private final Map<String, Conta> contas = new LinkedHashMap<>();
    private final Map<String, List<Lancamento>> lancamentos = new LinkedHashMap<>();

    public ContaRepository() {
        OffsetDateTime agora = OffsetDateTime.now(ZoneOffset.ofHours(-3));
        for (String clienteId : Seed.clienteIds()) {
            Random random = Seed.random("conta", clienteId);
            String sufixo = Seed.sufixo(clienteId);
            boolean premium = random.nextInt(3) == 0;
            adicionar(new Conta("cta-" + sufixo + "-1", clienteId, "0001", "1" + sufixo + "00-" + random.nextInt(10),
                    Conta.Tipo.CORRENTE, Conta.Status.ATIVA, Seed.valor(random, -200, premium ? 80_000 : 12_000),
                    Seed.valor(random, 0, 300), BigDecimal.valueOf(premium ? 10_000 : 1_500).setScale(2),
                    LocalDate.of(2015 + random.nextInt(10), 1 + random.nextInt(12), 1 + random.nextInt(28)),
                    premium ? CodigosProduto.CONTA_PREMIUM : CodigosProduto.CONTA_ESSENCIAL), LANCAMENTOS_CORRENTE, agora);
            if (random.nextBoolean()) {
                adicionar(new Conta("cta-" + sufixo + "-2", clienteId, "0001", "5" + sufixo + "00-" + random.nextInt(10),
                        Conta.Tipo.POUPANCA, Conta.Status.ATIVA, Seed.valor(random, 100, 40_000), BigDecimal.ZERO.setScale(2),
                        BigDecimal.ZERO.setScale(2), LocalDate.of(2016 + random.nextInt(9), 1 + random.nextInt(12), 1 + random.nextInt(28)),
                        CodigosProduto.POUPANCA), LANCAMENTOS_POUPANCA, agora);
            }
        }
    }

    public Optional<Conta> buscar(String id) {
        return Optional.ofNullable(contas.get(id));
    }

    public List<Conta> doCliente(String clienteId) {
        return contas.values().stream().filter(c -> c.clienteId().equals(clienteId)).toList();
    }

    public List<Lancamento> lancamentos(String contaId) {
        return lancamentos.getOrDefault(contaId, List.of());
    }

    private void adicionar(Conta conta, int quantidade, OffsetDateTime agora) {
        contas.put(conta.id(), conta);
        Random random = Seed.random("lancamentos", conta.id());
        List<Lancamento> lista = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            Categoria categoria = conta.tipo() == Conta.Tipo.POUPANCA
                    ? Categoria.RENDIMENTO
                    : Seed.escolher(random, List.of(Categoria.values()));
            boolean credito = switch (categoria) {
                case SALARIO, PIX_RECEBIDO, RENDIMENTO -> true;
                default -> false;
            };
            BigDecimal valor = switch (categoria) {
                case SALARIO -> Seed.valor(random, 3_000, 15_000);
                case RENDIMENTO -> Seed.valor(random, 1, 250);
                case TARIFA -> Seed.valor(random, 5, 50);
                default -> Seed.valor(random, 10, 2_500);
            };
            lista.add(new Lancamento(
                    conta.id() + "-l" + i,
                    conta.id(),
                    agora.minusMinutes(random.nextInt(DIAS_HISTORICO * 24 * 60)).withNano(0),
                    descricao(categoria, random),
                    credito ? valor : valor.negate(),
                    credito ? Lancamento.Tipo.CREDITO : Lancamento.Tipo.DEBITO,
                    categoria));
        }
        lista.sort(Comparator.comparing(Lancamento::dataHora).reversed());
        lancamentos.put(conta.id(), List.copyOf(lista));
    }

    private static String descricao(Categoria categoria, Random random) {
        return switch (categoria) {
            case SALARIO -> "Salário - Empresa Exemplo S.A.";
            case PIX_RECEBIDO -> "Pix recebido de " + Seed.escolher(random, List.of("Maria", "José", "Loja Online", "Paulo"));
            case PIX_ENVIADO -> "Pix enviado para " + Seed.escolher(random, List.of("Padaria", "Aluguel", "Academia", "Ana"));
            case COMPRA_DEBITO -> "Compra débito " + Seed.escolher(random, List.of("Supermercado", "Farmácia", "Posto", "Restaurante"));
            case BOLETO -> "Pagamento boleto " + Seed.escolher(random, List.of("Energia", "Internet", "Condomínio", "Escola"));
            case TARIFA -> "Tarifa pacote de serviços";
            case RENDIMENTO -> "Rendimento";
            case PARCELA_EMPRESTIMO -> "Débito parcela de empréstimo";
        };
    }
}
