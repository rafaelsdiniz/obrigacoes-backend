package br.com.obrigacoes.obrigacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResumoObrigacoesFiltro(
        @Schema(description = "Considera apenas as obrigações desta empresa")
        Long empresaId,
        @Schema(description = "Considera apenas as obrigações deste responsável")
        Long responsavelId
) {
}
