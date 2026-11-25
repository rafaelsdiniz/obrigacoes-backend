package br.com.obrigacoes.usuario.dto;

import br.com.obrigacoes.shared.validation.Atualizacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail informado é inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String email,

        @Size(max = 80, message = "O cargo deve ter no máximo 80 caracteres.")
        String cargo,

        @Schema(description = "Versão lida do registro. Obrigatória na atualização para evitar sobrescrever alterações de outra pessoa.")
        @NotNull(groups = Atualizacao.class, message = "A versão do registro é obrigatória na atualização.")
        Long versao
) {

    public UsuarioRequest {
        // Espaços nas pontas não devem invalidar o e-mail antes da normalização
        email = email == null ? null : email.strip();
    }
}
