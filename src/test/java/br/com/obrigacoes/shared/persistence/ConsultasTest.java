package br.com.obrigacoes.shared.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsultasTest {

    @Test
    void deveGerarPadraoContendoEmMinusculas() {
        assertThat(Consultas.contendo("Alfa")).isEqualTo("%alfa%");
    }

    @Test
    void deveTratarCuringasDigitadosComoTextoLiteral() {
        assertThat(Consultas.contendo("100%")).isEqualTo("%100\\%%");
        assertThat(Consultas.contendo("a_b")).isEqualTo("%a\\_b%");
        assertThat(Consultas.contendo("c:\\x")).isEqualTo("%c:\\\\x%");
    }
}
