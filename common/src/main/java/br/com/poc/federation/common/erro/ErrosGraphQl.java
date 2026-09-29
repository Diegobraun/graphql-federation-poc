package br.com.poc.federation.common.erro;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ErrosGraphQl {

    @GraphQlExceptionHandler
    public GraphQLError naoEncontrado(NaoEncontradoException erro, DataFetchingEnvironment env) {
        return erro(env, ErrorType.NOT_FOUND, erro);
    }

    @GraphQlExceptionHandler
    public GraphQLError acessoNegado(AcessoNegadoException erro, DataFetchingEnvironment env) {
        return erro(env, ErrorType.FORBIDDEN, erro);
    }

    @GraphQlExceptionHandler
    public GraphQLError requisicaoInvalida(IllegalArgumentException erro, DataFetchingEnvironment env) {
        return erro(env, ErrorType.BAD_REQUEST, erro);
    }

    private static GraphQLError erro(DataFetchingEnvironment env, ErrorType tipo, Throwable erro) {
        return GraphqlErrorBuilder.newError(env).errorType(tipo).message(erro.getMessage()).build();
    }
}
