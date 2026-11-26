package br.com.obrigacoes.anexo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;
import java.util.Objects;

@ConfigurationProperties(prefix = "app.anexos")
public record AnexosProperties(Path diretorio, DataSize tamanhoMaximo) {

    public AnexosProperties {
        Objects.requireNonNull(diretorio, "app.anexos.diretorio é obrigatório");
        Objects.requireNonNull(tamanhoMaximo, "app.anexos.tamanho-maximo é obrigatório");
        if (tamanhoMaximo.toBytes() <= 0) {
            throw new IllegalArgumentException("app.anexos.tamanho-maximo deve ser maior que zero");
        }
    }
}
