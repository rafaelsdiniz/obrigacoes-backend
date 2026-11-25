package br.com.obrigacoes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("API de Gerenciamento de Obrigações")
                .description("Gerenciamento de empresas, usuários responsáveis, obrigações e seus anexos.")
                .version("v1"));
    }
}
