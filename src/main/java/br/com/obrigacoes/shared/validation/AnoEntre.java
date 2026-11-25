package br.com.obrigacoes.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = AnoEntreValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface AnoEntre {

    int min();

    int max();

    String message() default "O ano deve estar entre {min} e {max}.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
