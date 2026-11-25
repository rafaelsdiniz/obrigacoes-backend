package br.com.obrigacoes.shared.domain;

import java.util.Locale;

/**
 * Normalização de textos recebidos nas requisições.
 */
public final class Textos {

    private Textos() {
    }

    /**
     * Remove espaços nas extremidades e converte texto vazio em {@code null},
     * evitando gravar strings em branco em campos opcionais.
     */
    public static String limparOuNulo(String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.strip();
        return limpo.isEmpty() ? null : limpo;
    }

    /**
     * Padroniza e-mails em minúsculas para que a verificação de unicidade não dependa da caixa.
     */
    public static String normalizarEmail(String email) {
        String limpo = limparOuNulo(email);
        return limpo == null ? null : limpo.toLowerCase(Locale.ROOT);
    }
}
