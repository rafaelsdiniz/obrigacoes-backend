package br.com.obrigacoes.usuario.dto;

import br.com.obrigacoes.shared.domain.Situacao;
import io.swagger.v3.oas.annotations.media.Schema;

public record UsuarioFiltro(
        @Schema(description = "Trecho do nome ou e-mail")
        String busca,
        Situacao situacao
) {
}
