package br.com.poc.federation.contas;

public final class Referencias {

    private Referencias() {
    }

    public record Cliente(String id) {
    }

    public record Produto(String codigo) {
    }
}
