package br.com.obrigacoes;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Statement;
import java.util.List;

/**
 * Base dos testes de integração: contexto completo da aplicação, MySQL real
 * via Testcontainers e banco limpo antes de cada teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class IntegracaoTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void limparBanco() {
        List<String> tabelas = jdbcTemplate.queryForList("""
                SELECT table_name FROM information_schema.tables
                WHERE table_schema = DATABASE() AND table_name <> 'flyway_schema_history'
                """, String.class);

        // Executado em uma única conexão, pois FOREIGN_KEY_CHECKS vale por sessão
        jdbcTemplate.execute((ConnectionCallback<Void>) conexao -> {
            try (Statement comando = conexao.createStatement()) {
                comando.execute("SET FOREIGN_KEY_CHECKS = 0");
                for (String tabela : tabelas) {
                    comando.execute("TRUNCATE TABLE " + tabela);
                }
                comando.execute("SET FOREIGN_KEY_CHECKS = 1");
            }
            return null;
        });
    }
}
