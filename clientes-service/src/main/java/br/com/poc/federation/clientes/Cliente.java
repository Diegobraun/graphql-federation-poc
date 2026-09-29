package br.com.poc.federation.clientes;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Cliente(
        String id,
        String nome,
        String cpf,
        String email,
        String telefone,
        LocalDate dataNascimento,
        Segmento segmento,
        BigDecimal rendaMensal,
        int scoreCredito,
        Endereco endereco,
        LocalDate clienteDesde) {

    public enum Segmento { VAREJO, ALTA_RENDA, PRIVATE }

    public record Endereco(String logradouro, String numero, String complemento, String bairro, String cidade, String uf, String cep) {
    }
}
