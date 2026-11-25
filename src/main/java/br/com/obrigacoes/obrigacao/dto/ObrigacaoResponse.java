package br.com.obrigacoes.obrigacao.dto;

import br.com.obrigacoes.empresa.Empresa;
import br.com.obrigacoes.obrigacao.Obrigacao;
import br.com.obrigacoes.obrigacao.StatusObrigacao;
import br.com.obrigacoes.obrigacao.TipoObrigacao;
import br.com.obrigacoes.shared.domain.Situacao;
import br.com.obrigacoes.usuario.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

public record ObrigacaoResponse(
        Long id,
        String titulo,
        String descricao,
        TipoObrigacao tipo,
        @Schema(type = "string", example = "2026-10")
        YearMonth competencia,
        LocalDate dataVencimento,
        StatusObrigacao status,
        @Schema(description = "Próximo status permitido no fluxo; nulo quando a obrigação está concluída")
        StatusObrigacao proximoStatus,
        boolean atrasada,
        EmpresaResumo empresa,
        ResponsavelResumo responsavel,
        Instant criadoEm,
        Instant atualizadoEm,
        Long versao
) {

    public static ObrigacaoResponse de(Obrigacao obrigacao, LocalDate hoje) {
        return new ObrigacaoResponse(
                obrigacao.getId(),
                obrigacao.getTitulo(),
                obrigacao.getDescricao(),
                obrigacao.getTipo(),
                obrigacao.getCompetencia(),
                obrigacao.getDataVencimento(),
                obrigacao.getStatus(),
                obrigacao.getStatus().proximo().orElse(null),
                obrigacao.isAtrasada(hoje),
                EmpresaResumo.de(obrigacao.getEmpresa()),
                ResponsavelResumo.de(obrigacao.getResponsavel()),
                obrigacao.getCriadoEm(),
                obrigacao.getAtualizadoEm(),
                obrigacao.getVersao()
        );
    }

    public record EmpresaResumo(Long id, String razaoSocial, String cnpj, Situacao situacao) {

        static EmpresaResumo de(Empresa empresa) {
            return new EmpresaResumo(empresa.getId(), empresa.getRazaoSocial(), empresa.getCnpj(), empresa.getSituacao());
        }
    }

    public record ResponsavelResumo(Long id, String nome, String email, Situacao situacao) {

        static ResponsavelResumo de(Usuario usuario) {
            return new ResponsavelResumo(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getSituacao());
        }
    }
}
