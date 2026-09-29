package br.com.poc.federation.produtos;

import br.com.poc.federation.produtos.Produto.Categoria;
import java.util.List;
import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ProdutoGraphQlController {

    private final ProdutoRepository repository;

    public ProdutoGraphQlController(ProdutoRepository repository) {
        this.repository = repository;
    }

    @QueryMapping
    public List<Produto> produtos(@Argument Categoria categoria, @Argument Boolean somenteAtivos) {
        return repository.listar(categoria, !Boolean.FALSE.equals(somenteAtivos));
    }

    @QueryMapping
    public Produto produto(@Argument String codigo) {
        return repository.buscar(codigo).orElse(null);
    }

    @EntityMapping("Produto")
    public List<Produto> referenciasProduto(@Argument List<String> codigo) {
        return codigo.stream().map(c -> repository.buscar(c).orElse(null)).toList();
    }
}
