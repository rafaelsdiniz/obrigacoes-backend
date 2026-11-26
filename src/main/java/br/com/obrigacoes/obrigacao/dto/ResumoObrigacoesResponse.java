package br.com.obrigacoes.obrigacao.dto;

import br.com.obrigacoes.obrigacao.ResumoObrigacoes;
import io.swagger.v3.oas.annotations.media.Schema;

public record ResumoObrigacoesResponse(
        long pendentes,
        long emAndamento,
        long concluidas,
        @Schema(description = "Obrigações não concluídas com vencimento anterior a hoje; também contadas em pendentes ou em andamento")
        long atrasadas
) {

    public static ResumoObrigacoesResponse de(ResumoObrigacoes resumo) {
        return new ResumoObrigacoesResponse(resumo.pendentes(), resumo.emAndamento(), resumo.concluidas(),
                resumo.atrasadas());
    }
}
