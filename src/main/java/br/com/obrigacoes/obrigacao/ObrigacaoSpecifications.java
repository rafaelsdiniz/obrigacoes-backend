package br.com.obrigacoes.obrigacao;

import br.com.obrigacoes.obrigacao.dto.ObrigacaoFiltro;
import br.com.obrigacoes.shared.domain.Textos;
import br.com.obrigacoes.shared.persistence.Consultas;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class ObrigacaoSpecifications {

    private ObrigacaoSpecifications() {
    }

    static Specification<Obrigacao> com(ObrigacaoFiltro filtro, LocalDate hoje) {
        List<Specification<Obrigacao>> condicoes = new ArrayList<>();

        if (filtro.status() != null) {
            condicoes.add((raiz, consulta, cb) -> cb.equal(raiz.get("status"), filtro.status()));
        }
        if (filtro.tipo() != null) {
            condicoes.add((raiz, consulta, cb) -> cb.equal(raiz.get("tipo"), filtro.tipo()));
        }
        if (filtro.empresaId() != null) {
            condicoes.add((raiz, consulta, cb) -> cb.equal(raiz.get("empresa").get("id"), filtro.empresaId()));
        }
        if (filtro.responsavelId() != null) {
            condicoes.add((raiz, consulta, cb) -> cb.equal(raiz.get("responsavel").get("id"), filtro.responsavelId()));
        }
        if (filtro.vencimentoDe() != null) {
            condicoes.add((raiz, consulta, cb) ->
                    cb.greaterThanOrEqualTo(raiz.get("dataVencimento"), filtro.vencimentoDe()));
        }
        if (filtro.vencimentoAte() != null) {
            condicoes.add((raiz, consulta, cb) ->
                    cb.lessThanOrEqualTo(raiz.get("dataVencimento"), filtro.vencimentoAte()));
        }
        if (filtro.atrasada() != null) {
            condicoes.add((raiz, consulta, cb) -> filtro.atrasada()
                    ? atrasada(raiz, cb, hoje)
                    : cb.not(atrasada(raiz, cb, hoje)));
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

    private static Predicate atrasada(Root<Obrigacao> raiz, CriteriaBuilder cb, LocalDate hoje) {
        return cb.and(
                cb.lessThan(raiz.get("dataVencimento"), hoje),
                cb.notEqual(raiz.get("status"), StatusObrigacao.CONCLUIDA)
        );
    }
}
