package br.com.obrigacoes.obrigacao.dto;

import br.com.obrigacoes.obrigacao.StatusObrigacao;
import jakarta.validation.constraints.NotNull;

public record AlteracaoStatusRequest(
        @NotNull(message = "O novo status é obrigatório.")
        StatusObrigacao status
) {
}
