package br.com.obrigacoes.shared.domain;

import java.util.Locale;

public final class Textos {

    private Textos() {
    }

    public static String limparOuNulo(String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.strip();
        return limpo.isEmpty() ? null : limpo;
    }

    public static String normalizarEmail(String email) {
        String limpo = limparOuNulo(email);
        return limpo == null ? null : limpo.toLowerCase(Locale.ROOT);
    }
}
