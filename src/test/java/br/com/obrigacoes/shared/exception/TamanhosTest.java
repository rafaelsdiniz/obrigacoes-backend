package br.com.obrigacoes.shared.exception;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.util.unit.DataSize;

import static org.assertj.core.api.Assertions.assertThat;

class TamanhosTest {

    @ParameterizedTest
    @CsvSource({"10MB,10 MB", "1024KB,1 MB", "1536KB,1536 KB", "1KB,1 KB", "1025B,1025 bytes", "700B,700 bytes"})
    void deveDescreverNaMaiorUnidadeExata(String tamanho, String esperado) {
        assertThat(Tamanhos.descrever(DataSize.parse(tamanho))).isEqualTo(esperado);
    }
}
