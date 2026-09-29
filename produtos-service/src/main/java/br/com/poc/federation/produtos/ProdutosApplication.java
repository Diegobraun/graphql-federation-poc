package br.com.poc.federation.produtos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.poc.federation")
public class ProdutosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProdutosApplication.class, args);
    }
}
