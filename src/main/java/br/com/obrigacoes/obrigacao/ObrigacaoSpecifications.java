package br.com.obrigacoes.obrigacao;

import br.com.obrigacoes.obrigacao.dto.ObrigacaoFiltro;
import br.com.obrigacoes.shared.domain.Textos;
import br.com.obrigacoes.shared.persistence.Consultas;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class ObrigacaoSpecifications {

    private ObrigacaoSpecifications() {
    }

    static Specification<Obrigacao> com(ObrigacaoFiltro filtro, LocalDate hoje) {
        List<Specification<Obrigacao>> condicoes = new ArrayList<>();

        if (filtro.status() != null && !filtro.status().isEmpty()) {
            condicoes.add((raiz, consulta, cb) -> raiz.get("status").in(filtro.status()));
        }
        if (filtro.tipo() != null) {
            condicoes.add((raiz, consulta, cb) -> cb.equal(raiz.get("tipo"), filtro.tipo()));
        }
        condicoes.add(doEscopo(filtro.empresaId(), filtro.responsavelId()));
        if (filtro.vencimentoDe() != null) {
            condicoes.add((raiz, consulta, cb) ->
                    cb.greaterThanOrEqualTo(raiz.get("dataVencimento"), filtro.vencimentoDe()));
        }
        if (filtro.vencimentoAte() != null) {
            condicoes.add((raiz, consulta, cb) ->
                    cb.lessThanOrEqualTo(raiz.get("dataVencimento"), filtro.vencimentoAte()));
        }
        if (filtro.atrasada() != null) {
            condicoes.add(filtro.atrasada() ? atrasadas(hoje) : Specification.not(atrasadas(hoje)));
        }

        String busca = Textos.limparOuNulo(filtro.busca());
        if (busca != null) {
            String termo = Consultas.contendo(busca);
            condicoes.add((raiz, consulta, cb) -> cb.or(
                    cb.like(cb.lower(raiz.get("titulo")), termo, Consultas.ESCAPE),
                    cb.like(cb.lower(raiz.get("descricao")), termo, Consultas.ESCAPE)
            ));
        }

        return Specification.allOf(condicoes);
    }

    static Specification<Obrigacao> doEscopo(Long empresaId, Long responsavelId) {
        List<Specification<Obrigacao>> condicoes = new ArrayList<>();
        if (empresaId != null) {
            condicoes.add((raiz, consulta, cb) -> cb.equal(raiz.get("empresa").get("id"), empresaId));
        }
        if (responsavelId != null) {
            condicoes.add((raiz, consulta, cb) -> cb.equal(raiz.get("responsavel").get("id"), responsavelId));
        }
        return Specification.allOf(condicoes);
    }

    static Specification<Obrigacao> comStatus(StatusObrigacao status) {
        return (raiz, consulta, cb) -> cb.equal(raiz.get("status"), status);
    }

    static Specification<Obrigacao> atrasadas(LocalDate hoje) {
        return (raiz, consulta, cb) -> cb.and(
                cb.lessThan(raiz.get("dataVencimento"), hoje),
                cb.notEqual(raiz.get("status"), StatusObrigacao.CONCLUIDA)
        );
    }
}
