package br.com.obrigacoes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "provedorDeDataHora")
public class JpaConfig {

    @Bean
    DateTimeProvider provedorDeDataHora(Clock clock) {
        return () -> Optional.of(Instant.now(clock).truncatedTo(ChronoUnit.MICROS));
    }
}
