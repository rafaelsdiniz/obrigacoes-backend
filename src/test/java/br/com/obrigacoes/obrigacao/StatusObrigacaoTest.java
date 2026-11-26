package br.com.obrigacoes.obrigacao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static br.com.obrigacoes.obrigacao.StatusObrigacao.CONCLUIDA;
import static br.com.obrigacoes.obrigacao.StatusObrigacao.EM_ANDAMENTO;
import static br.com.obrigacoes.obrigacao.StatusObrigacao.PENDENTE;
import static org.assertj.core.api.Assertions.assertThat;

class StatusObrigacaoTest {

    @ParameterizedTest(name = "{0} -> {1}: {2}")
    @CsvSource({
            "PENDENTE,     PENDENTE,     false",
            "PENDENTE,     EM_ANDAMENTO, true",
            "PENDENTE,     CONCLUIDA,    false",
            "EM_ANDAMENTO, PENDENTE,     false",
            "EM_ANDAMENTO, EM_ANDAMENTO, false",
            "EM_ANDAMENTO, CONCLUIDA,    true",
            "CONCLUIDA,    PENDENTE,     false",
            "CONCLUIDA,    EM_ANDAMENTO, false",
            "CONCLUIDA,    CONCLUIDA,    false"
    })
    void devePermitirApenasOAvancoParaOProximoStatus(StatusObrigacao origem, StatusObrigacao destino, boolean permitido) {
        assertThat(origem.podeAvancarPara(destino)).isEqualTo(permitido);
    }

    @Test
    void deveIndicarOProximoStatusDoFluxo() {
        assertThat(PENDENTE.proximo()).contains(EM_ANDAMENTO);
        assertThat(EM_ANDAMENTO.proximo()).contains(CONCLUIDA);
        assertThat(CONCLUIDA.proximo()).isEmpty();
    }

    @Test
    void somenteConcluidaEhStatusFinal() {
        assertThat(PENDENTE.isFinal()).isFalse();
        assertThat(EM_ANDAMENTO.isFinal()).isFalse();
        assertThat(CONCLUIDA.isFinal()).isTrue();
    }
}
