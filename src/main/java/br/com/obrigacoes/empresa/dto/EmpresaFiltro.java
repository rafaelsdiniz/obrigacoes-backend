package br.com.obrigacoes.empresa.dto;

import br.com.obrigacoes.shared.domain.Situacao;
import io.swagger.v3.oas.annotations.media.Schema;

public record EmpresaFiltro(
        @Schema(description = "Trecho da razão social, nome fantasia ou CNPJ")
        String busca,
        Situacao situacao
) {
}
