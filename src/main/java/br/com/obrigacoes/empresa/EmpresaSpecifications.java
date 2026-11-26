package br.com.obrigacoes.empresa;

import br.com.obrigacoes.empresa.dto.EmpresaFiltro;
import br.com.obrigacoes.shared.domain.Textos;
import br.com.obrigacoes.shared.persistence.Consultas;
import br.com.obrigacoes.shared.validation.Cnpjs;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class EmpresaSpecifications {

    private EmpresaSpecifications() {
    }

    static Specification<Empresa> com(EmpresaFiltro filtro) {
        List<Specification<Empresa>> condicoes = new ArrayList<>();

        if (filtro.situacao() != null) {
            condicoes.add((raiz, consulta, cb) -> cb.equal(raiz.get("situacao"), filtro.situacao()));
        }

        String busca = Textos.limparOuNulo(filtro.busca());
        if (busca != null) {
            String termo = Consultas.contendo(busca);
            String cnpjSemMascara = Cnpjs.normalizar(busca);
            condicoes.add((raiz, consulta, cb) -> {
                var nome = cb.or(
                        cb.like(cb.lower(raiz.get("razaoSocial")), termo, Consultas.ESCAPE),
                        cb.like(cb.lower(raiz.get("nomeFantasia")), termo, Consultas.ESCAPE));
                return cnpjSemMascara.isEmpty()
                        ? nome
                        : cb.or(nome, cb.like(cb.lower(raiz.get("cnpj")), Consultas.contendo(cnpjSemMascara), Consultas.ESCAPE));
            });
        }

        return Specification.allOf(condicoes);
    }
}
