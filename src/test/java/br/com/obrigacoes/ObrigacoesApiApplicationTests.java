package br.com.obrigacoes;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ObrigacoesApiApplicationTests extends IntegracaoTest {

    @Autowired
    private Flyway flyway;

    @Test
    void deveAplicarTodasAsMigrationsDoProjetoNaInicializacao() {
        MigrationInfo[] migrations = flyway.info().all();

        assertThat(migrations).isNotEmpty();
        assertThat(flyway.info().pending()).as("migrations pendentes").isEmpty();
        assertThat(migrations).allSatisfy(migration ->
                assertThat(migration.getState().isApplied()).as(migration.getScript()).isTrue());
    }
}
