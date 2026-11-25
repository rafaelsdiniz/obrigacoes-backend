package br.com.obrigacoes.obrigacao.dto;

import br.com.obrigacoes.obrigacao.StatusObrigacao;
import br.com.obrigacoes.obrigacao.TipoObrigacao;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ObrigacaoFiltro(
        @Schema(description = "Trecho do título ou da descrição")
        String busca,
        StatusObrigacao status,
        TipoObrigacao tipo,
        Long empresaId,
        Long responsavelId,
        @Schema(description = "Vencimento a partir desta data (inclusive)", example = "2026-11-01")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate vencimentoDe,
        @Schema(description = "Vencimento até esta data (inclusive)", example = "2026-11-30")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate vencimentoAte,
        @Schema(description = "true: somente atrasadas; false: somente em dia")
        Boolean atrasada
) {
}
