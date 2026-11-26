package br.com.obrigacoes.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

public class AnoEntreValidator implements ConstraintValidator<AnoEntre, TemporalAccessor> {

    private int minimo;
    private int maximo;

    @Override
    public void initialize(AnoEntre anotacao) {
        this.minimo = anotacao.min();
        this.maximo = anotacao.max();
    }

    @Override
    public boolean isValid(TemporalAccessor valor, ConstraintValidatorContext contexto) {
        if (valor == null) {
            return true;
        }
        int ano = valor.get(ChronoField.YEAR);
        return ano >= minimo && ano <= maximo;
    }
}
