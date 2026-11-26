package br.com.obrigacoes.anexo;

import br.com.obrigacoes.shared.exception.ArquivoInvalidoException;
import br.com.obrigacoes.shared.exception.ArquivoMuitoGrandeException;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@Component
class ValidadorArquivo {

    private final DataSize tamanhoMaximo;

    ValidadorArquivo(AnexosProperties propriedades) {
        this.tamanhoMaximo = propriedades.tamanhoMaximo();
    }

    ArquivoValidado validar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ArquivoInvalidoException("O arquivo é obrigatório e não pode estar vazio.");
        }
        if (arquivo.getSize() > tamanhoMaximo.toBytes()) {
            throw new ArquivoMuitoGrandeException(tamanhoMaximo);
        }
        String nome = NomesDeArquivo.sanitizar(arquivo.getOriginalFilename());
        TipoArquivo tipo = TipoArquivo.daExtensao(NomesDeArquivo.extensao(nome))
                .orElseThrow(ValidadorArquivo::tipoNaoPermitido);
        if (!reconhece(tipo, arquivo)) {
            throw new ArquivoInvalidoException(
                    "O conteúdo do arquivo não corresponde a um %s válido.".formatted(tipo.name()));
        }
        return new ArquivoValidado(nome, tipo);
    }

    private static ArquivoInvalidoException tipoNaoPermitido() {
        return new ArquivoInvalidoException(
                "Tipo de arquivo não permitido. Envie um arquivo %s.".formatted(TipoArquivo.descricaoDosPermitidos()));
    }

    private static boolean reconhece(TipoArquivo tipo, MultipartFile arquivo) {
        try {
            return tipo.reconhece(arquivo);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo enviado", e);
        }
    }

    record ArquivoValidado(String nome, TipoArquivo tipo) {
    }
}
