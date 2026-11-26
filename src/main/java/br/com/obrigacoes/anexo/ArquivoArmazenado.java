package br.com.obrigacoes.anexo;

import java.util.Objects;

record ArquivoArmazenado(String chave, long tamanho, String hashSha256) {

    ArquivoArmazenado {
        Objects.requireNonNull(chave, "chave é obrigatória");
        Objects.requireNonNull(hashSha256, "hashSha256 é obrigatório");
        if (tamanho <= 0) {
            throw new IllegalArgumentException("O arquivo armazenado não pode estar vazio.");
        }
    }
}
