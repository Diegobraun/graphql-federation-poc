package br.com.poc.federation.contas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.poc.federation")
public class ContasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContasApplication.class, args);
    }
}
