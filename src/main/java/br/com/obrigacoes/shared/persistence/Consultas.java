package br.com.obrigacoes.shared.persistence;

import java.util.Locale;

public final class Consultas {

    public static final char ESCAPE = '\\';

    private Consultas() {
    }

    public static String contendo(String termo) {
        String escapado = termo.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escapado + "%";
    }
}
