package br.com.obrigacoes.usuario;

import br.com.obrigacoes.shared.domain.Textos;
import br.com.obrigacoes.shared.persistence.Consultas;
import br.com.obrigacoes.usuario.dto.UsuarioFiltro;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Monta a consulta de usuários a partir dos filtros opcionais informados.
 */
final class UsuarioSpecifications {

    private UsuarioSpecifications() {
    }

    static Specification<Usuario> com(UsuarioFiltro filtro) {
        List<Specification<Usuario>> condicoes = new ArrayList<>();

        if (filtro.situacao() != null) {
            condicoes.add((raiz, consulta, cb) -> cb.equal(raiz.get("situacao"), filtro.situacao()));
        }

        String busca = Textos.limparOuNulo(filtro.busca());
        if (busca != null) {
            String termo = Consultas.contendo(busca);
            condicoes.add((raiz, consulta, cb) -> cb.or(
                    cb.like(cb.lower(raiz.get("nome")), termo, Consultas.ESCAPE),
                    cb.like(cb.lower(raiz.get("email")), termo, Consultas.ESCAPE)
            ));
        }

        return Specification.allOf(condicoes);
    }
}
