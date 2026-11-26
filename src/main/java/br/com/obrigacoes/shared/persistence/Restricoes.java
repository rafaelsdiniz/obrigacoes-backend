package br.com.obrigacoes.shared.persistence;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Locale;
import java.util.Optional;

public final class Restricoes {

    private Restricoes() {
    }

    public static boolean violou(DataIntegrityViolationException erro, String restricao) {
        String esperada = restricao.toLowerCase(Locale.ROOT);
        return nome(erro).map(nome -> nome.toLowerCase(Locale.ROOT).endsWith(esperada)).orElse(false);
    }

    public static Optional<String> nome(DataIntegrityViolationException erro) {
        for (Throwable causa = erro; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException violacao && violacao.getConstraintName() != null) {
                return Optional.of(violacao.getConstraintName());
            }
        }
        return Optional.empty();
    }
}
