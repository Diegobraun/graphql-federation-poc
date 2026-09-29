package br.com.poc.federation.emprestimos;

import java.math.BigDecimal;

public final class Referencias {

    private Referencias() {
    }

    public record Cliente(String id, BigDecimal rendaMensal, Integer scoreCredito) {

        public Cliente(String id) {
            this(id, null, null);
        }
    }

    public record Conta(String id) {
    }

    public record Produto(String codigo) {
    }
}
