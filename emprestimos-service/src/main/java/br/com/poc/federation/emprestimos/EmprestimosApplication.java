package br.com.poc.federation.emprestimos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.poc.federation")
public class EmprestimosApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmprestimosApplication.class, args);
    }
}
