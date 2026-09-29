package br.com.poc.federation.clientes;

import br.com.poc.federation.clientes.Cliente.Segmento;

public record FiltroClientes(String nome, Segmento segmento, String uf) {

    public boolean aceita(Cliente cliente) {
        return (nome == null || cliente.nome().toLowerCase().contains(nome.toLowerCase()))
                && (segmento == null || cliente.segmento() == segmento)
                && (uf == null || cliente.endereco().uf().equalsIgnoreCase(uf));
    }
}
