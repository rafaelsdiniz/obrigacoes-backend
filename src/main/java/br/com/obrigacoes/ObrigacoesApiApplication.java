package br.com.obrigacoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ObrigacoesApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObrigacoesApiApplication.class, args);
    }
}
