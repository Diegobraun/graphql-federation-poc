package br.com.poc.federation.produtos;

import static br.com.poc.federation.common.dados.CodigosProduto.*;

import br.com.poc.federation.produtos.Produto.Categoria;
import br.com.poc.federation.produtos.Produto.Indexador;
import br.com.poc.federation.produtos.Produto.Periodicidade;
import br.com.poc.federation.produtos.Produto.Tarifa;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProdutoRepository {

    private final Map<String, Produto> produtos = new LinkedHashMap<>();

    public ProdutoRepository() {
        adicionar(CONTA_ESSENCIAL, "Conta Corrente Essencial", Categoria.CONTA, null, null, null, null, "0",
                List.of(tarifa("Pacote de serviços", "0.00", Periodicidade.MENSAL), tarifa("TED", "0.00", Periodicidade.UNICA)));
        adicionar(CONTA_PREMIUM, "Conta Corrente Premium", Categoria.CONTA, null, null, null, null, "0",
                List.of(tarifa("Pacote de serviços", "49.90", Periodicidade.MENSAL), tarifa("Cartão adicional", "0.00", Periodicidade.ANUAL)));
        adicionar(POUPANCA, "Conta Poupança", Categoria.CONTA, Indexador.SELIC, "70", null, null, "0", List.of());
        adicionar(EMPRESTIMO_PESSOAL, "Empréstimo Pessoal", Categoria.EMPRESTIMO, Indexador.PRE, "3.49", 6, 48, "500",
                List.of(tarifa("IOF", "0.38", Periodicidade.UNICA)));
        adicionar(EMPRESTIMO_CONSIGNADO, "Crédito Consignado", Categoria.EMPRESTIMO, Indexador.PRE, "1.79", 12, 84, "1000",
                List.of(tarifa("IOF", "0.38", Periodicidade.UNICA)));
        adicionar(EMPRESTIMO_VEICULO, "Financiamento de Veículo", Categoria.EMPRESTIMO, Indexador.PRE, "1.99", 12, 60, "10000",
                List.of(tarifa("IOF", "0.38", Periodicidade.UNICA), tarifa("Avaliação do bem", "450.00", Periodicidade.UNICA)));
        adicionar(CDB_LIQUIDEZ, "CDB Liquidez Diária", Categoria.INVESTIMENTO, Indexador.CDI, "100", 0, 36, "1", List.of());
        adicionar(CDB_2_ANOS, "CDB 2 Anos", Categoria.INVESTIMENTO, Indexador.CDI, "112", 24, 24, "1000", List.of());
        adicionar(LCI_90, "LCI 90 dias", Categoria.INVESTIMENTO, Indexador.CDI, "93", 3, 24, "5000", List.of());
        adicionar(TESOURO_SELIC, "Tesouro Selic", Categoria.INVESTIMENTO, Indexador.SELIC, "100", 0, 72, "100",
                List.of(tarifa("Custódia B3", "0.20", Periodicidade.ANUAL)));
        adicionar(FUNDO_MULTIMERCADO, "Fundo Multimercado Macro", Categoria.INVESTIMENTO, Indexador.CDI, "120", 0, null, "500",
                List.of(tarifa("Taxa de administração", "1.50", Periodicidade.ANUAL), tarifa("Taxa de performance", "20.00", Periodicidade.ANUAL)));
    }

    public List<Produto> listar(Categoria categoria, boolean somenteAtivos) {
        return produtos.values().stream()
                .filter(p -> categoria == null || p.categoria() == categoria)
                .filter(p -> !somenteAtivos || p.ativo())
                .toList();
    }

    public Optional<Produto> buscar(String codigo) {
        return Optional.ofNullable(produtos.get(codigo));
    }

    private void adicionar(String codigo, String nome, Categoria categoria, Indexador indexador, String taxa,
                           Integer prazoMinimo, Integer prazoMaximo, String valorMinimo, List<Tarifa> tarifas) {
        produtos.put(codigo, new Produto(
                codigo,
                nome,
                categoria,
                nome + " para clientes pessoa física, contratação 100% digital.",
                indexador,
                taxa == null ? null : new BigDecimal(taxa),
                prazoMinimo,
                prazoMaximo,
                new BigDecimal(valorMinimo),
                true,
                tarifas,
                regulamento(nome),
                List.of("Termo de adesão", "Contrato padrão registrado em cartório", "Tabela de tarifas vigente", "Política de privacidade")));
    }

    private static Tarifa tarifa(String nome, String valor, Periodicidade periodicidade) {
        return new Tarifa(nome, new BigDecimal(valor), periodicidade);
    }

    private static String regulamento(String nome) {
        String clausula = "O presente regulamento disciplina as condições gerais do produto " + nome
                + ", incluindo direitos e deveres das partes, hipóteses de rescisão, encargos moratórios,"
                + " canais de atendimento, ouvidoria e tratamento de dados pessoais conforme a LGPD. ";
        return clausula.repeat(12);
    }
}
