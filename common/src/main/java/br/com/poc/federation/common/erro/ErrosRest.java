package br.com.poc.federation.common.erro;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrosRest {

    @ExceptionHandler(NaoEncontradoException.class)
    public ProblemDetail naoEncontrado(NaoEncontradoException erro) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, erro.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ProblemDetail acessoNegado(AcessoNegadoException erro) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, erro.getMessage());
    }
}
