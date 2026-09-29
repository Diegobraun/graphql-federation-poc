package br.com.poc.federation.clientes;

import br.com.poc.federation.common.dados.Seed;
import br.com.poc.federation.common.erro.NaoEncontradoException;
import br.com.poc.federation.common.seguranca.Acesso;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteRestController {

    private final ClienteRepository repository;
    private final Acesso acesso;

    public ClienteRestController(ClienteRepository repository, Acesso acesso) {
        this.repository = repository;
        this.acesso = acesso;
    }

    @GetMapping("/{id}")
    public ClienteCompleto buscar(@PathVariable String id) {
        acesso.exigirAcessoAoCliente(id);
        Cliente cliente = repository.buscar(id).orElseThrow(() -> new NaoEncontradoException("Cliente", id));
        return ClienteCompleto.de(cliente);
    }

    public record ClienteCompleto(
            Cliente cliente,
            List<Documento> documentos,
            DadosProfissionais dadosProfissionais,
            Map<String, Boolean> preferencias,
            List<Consentimento> consentimentosLgpd,
            List<Alteracao> historicoAlteracoes,
            boolean pessoaPoliticamenteExposta,
            boolean residenteFiscalExterior) {

        static ClienteCompleto de(Cliente cliente) {
            Random random = Seed.random("rest-cliente", cliente.id());
            var documentos = List.of(
                    new Documento("RG", "%09d".formatted(random.nextInt(999_999_999)), "SSP", cliente.endereco().uf(), LocalDate.of(2015, 3, 10)),
                    new Documento("CNH", "%011d".formatted(Math.abs(random.nextLong() % 99_999_999_999L)), "DETRAN", cliente.endereco().uf(), LocalDate.of(2019, 7, 22)));
            var profissionais = new DadosProfissionais("Empresa Exemplo S.A.", "Analista", LocalDate.of(2018, 2, 1), "CLT", cliente.rendaMensal());
            Map<String, Boolean> preferencias = new LinkedHashMap<>();
            List.of("notificacaoPush", "notificacaoEmail", "notificacaoSms", "extratoPapel", "ofertasParceiros", "modoEscuro",
                            "biometria", "limiteNoturnoReduzido", "compartilharDadosOpenFinance", "receberNewsletter",
                            "alertaSaldoBaixo", "alertaCompraInternacional", "tokenDigital", "atendimentoWhatsapp", "fatura_digital")
                    .forEach(p -> preferencias.put(p, random.nextBoolean()));
            var consentimentos = IntStream.range(0, 6)
                    .mapToObj(i -> new Consentimento("FINALIDADE_" + i, "v" + (1 + i % 3) + ".0", true,
                            OffsetDateTime.of(2023, 1 + i, 5, 10, 0, 0, 0, ZoneOffset.ofHours(-3)), "APP"))
                    .toList();
            var historico = IntStream.range(0, 15)
                    .mapToObj(i -> new Alteracao(OffsetDateTime.of(2024, 1 + i % 12, 1 + i, 9, 30, 0, 0, ZoneOffset.ofHours(-3)),
                            List.of("email", "telefone", "endereco.logradouro", "rendaMensal").get(i % 4),
                            "valor anterior " + i, "valor novo " + i, i % 2 == 0 ? "APP" : "AGENCIA", "usuario.sistema." + i))
                    .toList();
            return new ClienteCompleto(cliente, documentos, profissionais, preferencias, consentimentos, historico, false, false);
        }
    }

    public record Documento(String tipo, String numero, String orgaoEmissor, String uf, LocalDate dataEmissao) {
    }

    public record DadosProfissionais(String empresa, String cargo, LocalDate admissao, String regime, BigDecimal rendaComprovada) {
    }

    public record Consentimento(String finalidade, String versaoTermo, boolean aceito, OffsetDateTime dataHora, String canal) {
    }

    public record Alteracao(OffsetDateTime dataHora, String campo, String valorAnterior, String valorNovo, String canal, String usuario) {
    }
}
