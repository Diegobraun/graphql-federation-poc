package br.com.poc.federation.investimentos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.poc.federation")
public class InvestimentosApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestimentosApplication.class, args);
    }
}
