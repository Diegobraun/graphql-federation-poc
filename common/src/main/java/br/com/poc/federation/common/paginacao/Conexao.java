package br.com.poc.federation.common.paginacao;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

public record Conexao<T>(List<Aresta<T>> edges, InfoPagina pageInfo, int totalCount) {

    public record Aresta<T>(String cursor, T node) {
    }

    public record InfoPagina(boolean hasNextPage, boolean hasPreviousPage, String startCursor, String endCursor) {
    }

    public static <T> Conexao<T> paginar(List<T> itens, Integer first, String after, Function<T, String> chave, int limite) {
        int tamanho = Math.min(first == null ? limite : Math.max(first, 0), limite);
        int inicio = 0;
        if (after != null) {
            String chaveApos = decodificar(after);
            for (int i = 0; i < itens.size(); i++) {
                if (chave.apply(itens.get(i)).equals(chaveApos)) {
                    inicio = i + 1;
                    break;
                }
            }
        }
        int fim = Math.min(inicio + tamanho, itens.size());
        List<Aresta<T>> arestas = itens.subList(inicio, fim).stream()
                .map(item -> new Aresta<>(codificar(chave.apply(item)), item))
                .toList();
        var info = new InfoPagina(
                fim < itens.size(),
                inicio > 0,
                arestas.isEmpty() ? null : arestas.getFirst().cursor(),
                arestas.isEmpty() ? null : arestas.getLast().cursor());
        return new Conexao<>(arestas, info, itens.size());
    }

    private static String codificar(String valor) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodificar(String cursor) {
        try {
            return new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Cursor inválido: " + cursor);
        }
    }
}
