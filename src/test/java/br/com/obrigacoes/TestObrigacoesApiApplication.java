package br.com.obrigacoes;

import org.springframework.boot.SpringApplication;

/**
 * Executa a aplicação localmente com um MySQL em container,
 * sem precisar do docker-compose: {@code ./mvnw spring-boot:test-run}.
 */
public class TestObrigacoesApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(ObrigacoesApiApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
