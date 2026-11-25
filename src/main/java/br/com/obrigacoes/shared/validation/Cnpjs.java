package br.com.obrigacoes.shared.validation;

import java.util.Locale;
import java.util.regex.Pattern;

public final class Cnpjs {

    private static final Pattern FORMATO = Pattern.compile("[0-9A-Z]{12}[0-9]{2}");
    private static final Pattern MASCARA = Pattern.compile("[./\\-\\s]");
    private static final int[] PESOS_PRIMEIRO_DV = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_SEGUNDO_DV = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private Cnpjs() {
    }

    public static String normalizar(String cnpj) {
        return cnpj == null ? null : MASCARA.matcher(cnpj).replaceAll("").toUpperCase(Locale.ROOT);
    }

    public static boolean isValido(String cnpj) {
        String valor = normalizar(cnpj);
        if (valor == null || !FORMATO.matcher(valor).matches() || todosCaracteresIguais(valor)) {
            return false;
        }
        int primeiroDv = calcularDigito(valor, PESOS_PRIMEIRO_DV);
        int segundoDv = calcularDigito(valor, PESOS_SEGUNDO_DV);
        return valor.charAt(12) - '0' == primeiroDv && valor.charAt(13) - '0' == segundoDv;
    }

    private static int calcularDigito(String cnpj, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (cnpj.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean todosCaracteresIguais(String valor) {
        return valor.chars().allMatch(caractere -> caractere == valor.charAt(0));
    }
}
