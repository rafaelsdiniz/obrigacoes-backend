package br.com.obrigacoes.anexo;

import java.util.Locale;
import java.util.regex.Pattern;

final class NomesDeArquivo {

    static final int TAMANHO_MAXIMO = 255;

    private static final Pattern CARACTERES_INVISIVEIS = Pattern.compile("[\\p{Cc}\\p{Cf}\\p{Zl}\\p{Zp}]");

    private NomesDeArquivo() {
    }

    static String sanitizar(String nomeEnviado) {
        if (nomeEnviado == null) {
            return "";
        }
        int fimDoCaminho = Math.max(nomeEnviado.lastIndexOf('/'), nomeEnviado.lastIndexOf('\\'));
        String semCaminho = nomeEnviado.substring(fimDoCaminho + 1);
        String nome = CARACTERES_INVISIVEIS.matcher(semCaminho).replaceAll("").strip();
        return nome.length() <= TAMANHO_MAXIMO ? nome : truncar(nome);
    }

    static String extensao(String nome) {
        int ponto = nome.lastIndexOf('.');
        return ponto < 0 || ponto == nome.length() - 1 ? "" : nome.substring(ponto + 1).toLowerCase(Locale.ROOT);
    }

    private static String truncar(String nome) {
        String extensao = extensao(nome);
        int tamanhoDoSufixo = extensao.isEmpty() ? 0 : extensao.length() + 1;
        String sufixo = tamanhoDoSufixo < TAMANHO_MAXIMO ? nome.substring(nome.length() - tamanhoDoSufixo) : "";
        int corte = TAMANHO_MAXIMO - sufixo.length();
        if (Character.isHighSurrogate(nome.charAt(corte - 1))) {
            corte--;
        }
        return nome.substring(0, corte) + sufixo;
    }
}
