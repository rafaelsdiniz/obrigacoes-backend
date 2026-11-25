package br.com.obrigacoes.shared.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class AnoEntreValidatorTest {

    private static ValidatorFactory fabrica;
    private static Validator validador;

    record Periodo(@AnoEntre(min = 2000, max = 2100) LocalDate data,
                   @AnoEntre(min = 2000, max = 2100) YearMonth competencia) {
    }

    @BeforeAll
    static void criarValidador() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validador = fabrica.getValidator();
    }

    @AfterAll
    static void fecharFabrica() {
        fabrica.close();
    }

    @Test
    void deveAceitarLimitesEValoresAusentes() {
        assertThat(validador.validate(new Periodo(LocalDate.of(2000, 1, 1), YearMonth.of(2100, 12)))).isEmpty();
        assertThat(validador.validate(new Periodo(null, null))).isEmpty();
    }

    @Test
    void deveRejeitarAnosForaDoIntervalo() {
        assertThat(validador.validate(new Periodo(LocalDate.of(1999, 12, 31), YearMonth.of(2101, 1))))
                .extracting(violacao -> violacao.getPropertyPath().toString())
                .containsExactlyInAnyOrder("data", "competencia");
    }
}
