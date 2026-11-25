package br.com.obrigacoes.usuario.dto;

import br.com.obrigacoes.shared.domain.Situacao;
import br.com.obrigacoes.usuario.Usuario;

import java.time.Instant;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String cargo,
        Situacao situacao,
        Instant criadoEm,
        Instant atualizadoEm,
        Long versao
) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCargo(),
                usuario.getSituacao(),
                usuario.getCriadoEm(),
                usuario.getAtualizadoEm(),
                usuario.getVersao()
        );
    }
}
