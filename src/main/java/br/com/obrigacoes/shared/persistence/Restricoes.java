package br.com.obrigacoes.shared.persistence;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Locale;

public final class Restricoes {

    private Restricoes() {
    }

    public static boolean violou(DataIntegrityViolationException erro, String restricao) {
        String esperada = restricao.toLowerCase(Locale.ROOT);
        for (Throwable causa = erro; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException violacao && violacao.getConstraintName() != null
                    && violacao.getConstraintName().toLowerCase(Locale.ROOT).endsWith(esperada)) {
                return true;
            }
        }
        return false;
    }
}
