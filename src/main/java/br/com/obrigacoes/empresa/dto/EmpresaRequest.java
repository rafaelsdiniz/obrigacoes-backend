package br.com.obrigacoes.empresa.dto;

import br.com.obrigacoes.shared.validation.Atualizacao;
import br.com.obrigacoes.shared.validation.CnpjValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmpresaRequest(
        @NotBlank(message = "A razão social é obrigatória.")
        @Size(max = 150, message = "A razão social deve ter no máximo 150 caracteres.")
        String razaoSocial,

        @Size(max = 150, message = "O nome fantasia deve ter no máximo 150 caracteres.")
        String nomeFantasia,

        @Schema(description = "CNPJ numérico ou alfanumérico, com ou sem máscara", example = "11.222.333/0001-81")
        @NotBlank(message = "O CNPJ é obrigatório.")
        @CnpjValido
        String cnpj,

        @Email(message = "O e-mail informado é inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String email,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
        String telefone,

        @Schema(description = "Versão lida do registro. Obrigatória na atualização para evitar sobrescrever alterações de outra pessoa.")
        @NotNull(groups = Atualizacao.class, message = "A versão do registro é obrigatória na atualização.")
        Long versao
) {

    public EmpresaRequest {
        // Espaços nas pontas não devem invalidar o e-mail antes da normalização
        email = email == null ? null : email.strip();
    }
}
