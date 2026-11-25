package br.com.obrigacoes.shared.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class CnpjsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "11222333000181",
            "11.222.333/0001-81",
            "11444777000161",
            "12ABC34501DE35",
            "12.ABC.345/01DE-35",
            "12.abc.345/01de-35"
    })
    void deveAceitarCnpjNumericoEAlfanumericoComOuSemMascara(String cnpj) {
        assertThat(Cnpjs.isValido(cnpj)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "11222333000182",
            "12ABC34501DE36",
            "00000000000000",
            "AAAAAAAAAAAA00",
            "1122233300018",
            "112223330001811",
            "12ABC34501DEA5",
            "11.222.333/0001-8X"
    })
    void deveRejeitarCnpjComDigitoErradoFormatoInvalidoOuRepetido(String cnpj) {
        assertThat(Cnpjs.isValido(cnpj)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void deveRejeitarCnpjAusente(String cnpj) {
        assertThat(Cnpjs.isValido(cnpj)).isFalse();
    }

    @Test
    void deveRemoverMascaraEConverterParaMaiusculas() {
        assertThat(Cnpjs.normalizar(" 12.abc.345/01de-35 ")).isEqualTo("12ABC34501DE35");
    }

    @Test
    void deveValidarIndependenteDoIdiomaPadraoDoServidor() {
        Locale original = Locale.getDefault();
        try {
            // No turco, "i".toUpperCase() vira "İ", o que invalidaria CNPJs com a letra I
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertThat(Cnpjs.normalizar("12.ibc.345/01de-10")).isEqualTo("12IBC34501DE10");
            assertThat(Cnpjs.isValido("12.ibc.345/01de-10")).isTrue();
        } finally {
            Locale.setDefault(original);
        }
    }
}
