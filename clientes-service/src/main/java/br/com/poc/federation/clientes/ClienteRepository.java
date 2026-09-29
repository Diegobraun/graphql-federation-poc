package br.com.poc.federation.clientes;

import br.com.poc.federation.clientes.Cliente.Endereco;
import br.com.poc.federation.clientes.Cliente.Segmento;
import br.com.poc.federation.common.dados.Seed;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.springframework.stereotype.Repository;

@Repository
public class ClienteRepository {

    private static final List<String> NOMES = List.of(
            "Ana", "Bruno", "Carla", "Diego", "Eduarda", "Felipe", "Gabriela", "Henrique", "Isabela", "João",
            "Karina", "Lucas", "Mariana", "Nicolas", "Olívia", "Pedro", "Quésia", "Rafael", "Sofia", "Thiago",
            "Úrsula", "Vinícius", "Wagner", "Xavier", "Yasmin", "Zeca", "Amanda", "Bernardo", "Camila", "Daniel");
    private static final List<String> SOBRENOMES = List.of(
            "Silva", "Souza", "Oliveira", "Santos", "Pereira", "Lima", "Carvalho", "Ferreira", "Almeida", "Costa", "Braun", "Ribeiro");
    private static final List<String[]> CIDADES = List.of(
            new String[] {"São Paulo", "SP"}, new String[] {"Rio de Janeiro", "RJ"}, new String[] {"Belo Horizonte", "MG"},
            new String[] {"Porto Alegre", "RS"}, new String[] {"Curitiba", "PR"}, new String[] {"Recife", "PE"},
            new String[] {"Salvador", "BA"}, new String[] {"Florianópolis", "SC"});
    private static final List<String> RUAS = List.of("Rua das Flores", "Av. Paulista", "Rua XV de Novembro", "Av. Atlântica", "Rua da Consolação", "Av. Brasil");
    private static final List<String> BAIRROS = List.of("Centro", "Jardins", "Moinhos de Vento", "Savassi", "Boa Viagem", "Batel");

    private final Map<String, Cliente> clientes = new LinkedHashMap<>();

    public ClienteRepository() {
        List<String> ids = Seed.clienteIds();
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            clientes.put(id, gerar(id, NOMES.get(i)));
        }
    }

    public Optional<Cliente> buscar(String id) {
        return Optional.ofNullable(clientes.get(id));
    }

    public List<Cliente> listar(FiltroClientes filtro) {
        return clientes.values().stream().filter(c -> filtro == null || filtro.aceita(c)).toList();
    }

    private static Cliente gerar(String id, String primeiroNome) {
        Random random = Seed.random("cliente", id);
        String nome = primeiroNome + " " + Seed.escolher(random, SOBRENOMES) + " " + Seed.escolher(random, SOBRENOMES);
        BigDecimal renda = BigDecimal.valueOf(2_000 + Math.pow(random.nextDouble(), 2) * 58_000).setScale(2, RoundingMode.HALF_EVEN);
        String[] cidade = Seed.escolher(random, CIDADES);
        var endereco = new Endereco(
                Seed.escolher(random, RUAS),
                String.valueOf(10 + random.nextInt(2000)),
                random.nextBoolean() ? "Apto " + (11 + random.nextInt(190)) : null,
                Seed.escolher(random, BAIRROS),
                cidade[0],
                cidade[1],
                "%05d-%03d".formatted(random.nextInt(99_999), random.nextInt(999)));
        return new Cliente(
                id,
                nome,
                cpf(random),
                primeiroNome.toLowerCase().replaceAll("[^a-z]", "") + "." + Seed.sufixo(id) + "@email.com",
                "(11) 9%04d-%04d".formatted(random.nextInt(10_000), random.nextInt(10_000)),
                LocalDate.of(1960 + random.nextInt(45), 1 + random.nextInt(12), 1 + random.nextInt(28)),
                segmento(renda),
                renda,
                300 + random.nextInt(651),
                endereco,
                LocalDate.of(2012 + random.nextInt(13), 1 + random.nextInt(12), 1 + random.nextInt(28)));
    }

    private static Segmento segmento(BigDecimal renda) {
        if (renda.compareTo(BigDecimal.valueOf(30_000)) >= 0) {
            return Segmento.PRIVATE;
        }
        return renda.compareTo(BigDecimal.valueOf(10_000)) >= 0 ? Segmento.ALTA_RENDA : Segmento.VAREJO;
    }

    private static String cpf(Random random) {
        int[] d = new int[11];
        for (int i = 0; i < 9; i++) {
            d[i] = random.nextInt(10);
        }
        d[9] = digitoVerificador(d, 9);
        d[10] = digitoVerificador(d, 10);
        return "%d%d%d.%d%d%d.%d%d%d-%d%d".formatted(d[0], d[1], d[2], d[3], d[4], d[5], d[6], d[7], d[8], d[9], d[10]);
    }

    private static int digitoVerificador(int[] d, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += d[i] * (tamanho + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
