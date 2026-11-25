package br.com.obrigacoes.shared.persistence;

import java.util.Locale;

/**
 * Utilitários para montar filtros de texto em consultas JPA.
 */
public final class Consultas {

    /**
     * Caractere de escape usado nos padrões gerados por {@link #contendo(String)}.
     */
    public static final char ESCAPE = '\\';

    private Consultas() {
    }

    /**
     * Gera o padrão LIKE "contém o termo", em minúsculas, tratando {@code %} e {@code _}
     * digitados pelo usuário como texto literal e não como curingas.
     */
    public static String contendo(String termo) {
        String escapado = termo.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escapado + "%";
    }
}
