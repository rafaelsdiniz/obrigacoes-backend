package br.com.obrigacoes.obrigacao.dto;

import br.com.obrigacoes.obrigacao.TipoObrigacao;
import br.com.obrigacoes.shared.validation.AnoEntre;
import br.com.obrigacoes.shared.validation.Atualizacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.YearMonth;

public record ObrigacaoRequest(
        @NotBlank(message = "O título é obrigatório.")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
        String titulo,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres.")
        String descricao,

        @NotNull(message = "O tipo é obrigatório.")
        TipoObrigacao tipo,

        @Schema(type = "string", description = "Mês de competência no formato AAAA-MM", example = "2026-10")
        @AnoEntre(min = ANO_MINIMO, max = ANO_MAXIMO, message = "A competência deve estar entre os anos {min} e {max}.")
        YearMonth competencia,

        @Schema(example = "2026-11-20")
        @NotNull(message = "A data de vencimento é obrigatória.")
        @AnoEntre(min = ANO_MINIMO, max = ANO_MAXIMO, message = "A data de vencimento deve estar entre os anos {min} e {max}.")
        LocalDate dataVencimento,

        @NotNull(message = "A empresa é obrigatória.")
        Long empresaId,

        @NotNull(message = "O responsável é obrigatório.")
        Long responsavelId,

        @Schema(description = "Versão lida do registro. Obrigatória na atualização para evitar sobrescrever alterações de outra pessoa.")
        @NotNull(groups = Atualizacao.class, message = "A versão do registro é obrigatória na atualização.")
        Long versao
) {

    public static final int ANO_MINIMO = 2000;
    public static final int ANO_MAXIMO = 2100;
}
