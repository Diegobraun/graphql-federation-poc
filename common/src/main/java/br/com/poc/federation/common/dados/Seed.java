package br.com.poc.federation.common.dados;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public final class Seed {

    public static final int TOTAL_CLIENTES = 30;

    private Seed() {
    }

    public static List<String> clienteIds() {
        return IntStream.rangeClosed(1, TOTAL_CLIENTES).mapToObj(i -> "cli-%03d".formatted(i)).toList();
    }

    public static String sufixo(String clienteId) {
        return clienteId.substring(clienteId.indexOf('-') + 1);
    }

    public static Random random(String... partes) {
        return new Random(String.join("|", partes).hashCode());
    }

    public static BigDecimal valor(Random random, double minimo, double maximo) {
        return BigDecimal.valueOf(minimo + random.nextDouble() * (maximo - minimo)).setScale(2, RoundingMode.HALF_EVEN);
    }

    public static <T> T escolher(Random random, List<T> opcoes) {
        return opcoes.get(random.nextInt(opcoes.size()));
    }
}
