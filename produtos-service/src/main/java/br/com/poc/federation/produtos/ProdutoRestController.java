package br.com.poc.federation.produtos;

import br.com.poc.federation.common.erro.NaoEncontradoException;
import br.com.poc.federation.produtos.Produto.Categoria;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/produtos")
public class ProdutoRestController {

    private final ProdutoRepository repository;

    public ProdutoRestController(ProdutoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Produto> listar(@RequestParam(required = false) Categoria categoria) {
        return repository.listar(categoria, false);
    }

    @GetMapping("/{codigo}")
    public Produto buscar(@PathVariable String codigo) {
        return repository.buscar(codigo).orElseThrow(() -> new NaoEncontradoException("Produto", codigo));
    }
}
