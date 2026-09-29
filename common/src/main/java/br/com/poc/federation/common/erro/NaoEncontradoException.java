package br.com.poc.federation.common.erro;

public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String tipo, String id) {
        super(tipo + " não encontrado: " + id);
    }
}
