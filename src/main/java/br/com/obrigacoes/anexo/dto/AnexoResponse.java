package br.com.obrigacoes.anexo.dto;

import br.com.obrigacoes.anexo.Anexo;
import br.com.obrigacoes.anexo.TipoArquivo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record AnexoResponse(
        Long id,
        String nomeOriginal,
        TipoArquivo tipo,
        @Schema(example = "application/pdf")
        String tipoConteudo,
        @Schema(description = "Tamanho em bytes")
        long tamanho,
        @Schema(description = "Hash SHA-256 do conteúdo, em hexadecimal")
        String hashSha256,
        Instant criadoEm
) {

    public static AnexoResponse de(Anexo anexo) {
        return new AnexoResponse(
                anexo.getId(),
                anexo.getNomeOriginal(),
                anexo.getTipo(),
                anexo.getTipo().getTipoConteudo(),
                anexo.getTamanho(),
                anexo.getHashSha256(),
                anexo.getCriadoEm()
        );
    }
}
