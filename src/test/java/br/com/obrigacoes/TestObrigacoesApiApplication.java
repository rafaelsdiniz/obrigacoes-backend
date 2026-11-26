package br.com.obrigacoes;

import org.springframework.boot.SpringApplication;

public class TestObrigacoesApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(ObrigacoesApiApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
