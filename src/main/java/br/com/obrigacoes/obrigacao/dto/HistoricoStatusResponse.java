package br.com.obrigacoes.obrigacao.dto;

import br.com.obrigacoes.obrigacao.HistoricoStatusObrigacao;
import br.com.obrigacoes.obrigacao.StatusObrigacao;

import java.time.Instant;

public record HistoricoStatusResponse(
        StatusObrigacao statusAnterior,
        StatusObrigacao statusNovo,
        Instant alteradoEm
) {

    public static HistoricoStatusResponse de(HistoricoStatusObrigacao historico) {
        return new HistoricoStatusResponse(historico.getStatusAnterior(), historico.getStatusNovo(), historico.getAlteradoEm());
    }
}
