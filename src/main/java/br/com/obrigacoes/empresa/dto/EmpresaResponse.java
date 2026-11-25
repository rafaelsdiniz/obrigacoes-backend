package br.com.obrigacoes.empresa.dto;

import br.com.obrigacoes.empresa.Empresa;
import br.com.obrigacoes.shared.domain.Situacao;

import java.time.Instant;

public record EmpresaResponse(
        Long id,
        String razaoSocial,
        String nomeFantasia,
        String cnpj,
        String email,
        String telefone,
        Situacao situacao,
        Instant criadoEm,
        Instant atualizadoEm,
        Long versao
) {

    public static EmpresaResponse de(Empresa empresa) {
        return new EmpresaResponse(
                empresa.getId(),
                empresa.getRazaoSocial(),
                empresa.getNomeFantasia(),
                empresa.getCnpj(),
                empresa.getEmail(),
                empresa.getTelefone(),
                empresa.getSituacao(),
                empresa.getCriadoEm(),
                empresa.getAtualizadoEm(),
                empresa.getVersao()
        );
    }
}
