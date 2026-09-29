package br.com.poc.federation.contas;

import br.com.poc.federation.common.dados.Seed;
import br.com.poc.federation.common.erro.NaoEncontradoException;
import br.com.poc.federation.common.seguranca.Acesso;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContaRestController {

    private final ContaRepository repository;
    private final Acesso acesso;

    public ContaRestController(ContaRepository repository, Acesso acesso) {
        this.repository = repository;
        this.acesso = acesso;
    }

    @GetMapping("/api/v1/clientes/{clienteId}/contas")
    public List<ContaCompleta> doCliente(@PathVariable String clienteId) {
        acesso.exigirAcessoAoCliente(clienteId);
        return repository.doCliente(clienteId).stream().map(this::completa).toList();
    }

    @GetMapping("/api/v1/contas/{id}")
    public ContaCompleta buscar(@PathVariable String id) {
        Conta conta = repository.buscar(id).orElseThrow(() -> new NaoEncontradoException("Conta", id));
        acesso.exigirAcessoAoCliente(conta.clienteId());
        return completa(conta);
    }

    private ContaCompleta completa(Conta conta) {
        Random random = Seed.random("rest-conta", conta.id());
        var chaves = List.of(
                new ChavePix("CPF", "***.***.***-**", LocalDate.of(2021, 3, 1)),
                new ChavePix("EMAIL", "cliente" + Seed.sufixo(conta.clienteId()) + "@email.com", LocalDate.of(2021, 3, 1)),
                new ChavePix("ALEATORIA", UUID.nameUUIDFromBytes(conta.id().getBytes()).toString(), LocalDate.of(2022, 8, 15)));
        var cartoes = IntStream.range(0, 2)
                .mapToObj(i -> new Cartao("**** **** **** " + (1000 + random.nextInt(9000)), i == 0 ? "DEBITO" : "MULTIPLO",
                        "ATIVO", LocalDate.of(2029, 1 + i, 1), BigDecimal.valueOf(5_000 + 1_000L * i)))
                .toList();
        return new ContaCompleta(conta, conta.saldo(), repository.lancamentos(conta.id()), chaves, cartoes,
                new Tarifas(new BigDecimal("0.00"), new BigDecimal("12.50"), new BigDecimal("7.90")));
    }

    public record ContaCompleta(Conta conta, Conta.Saldo saldo, List<Lancamento> extrato, List<ChavePix> chavesPix,
                                List<Cartao> cartoes, Tarifas tarifas) {
    }

    public record ChavePix(String tipo, String valor, LocalDate cadastradaEm) {
    }

    public record Cartao(String numeroMascarado, String funcao, String status, LocalDate validade, BigDecimal limite) {
    }

    public record Tarifas(BigDecimal ted, BigDecimal saqueExtra, BigDecimal segundaViaCartao) {
    }
}
