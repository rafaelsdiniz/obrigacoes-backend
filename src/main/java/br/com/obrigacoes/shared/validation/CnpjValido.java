package br.com.obrigacoes.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida um CNPJ numérico ou alfanumérico, com ou sem máscara.
 * Valores nulos são considerados válidos; combine com {@code @NotBlank} quando o campo for obrigatório.
 */
@Documented
@Constraint(validatedBy = CnpjValidoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface CnpjValido {

    String message() default "CNPJ inválido.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
