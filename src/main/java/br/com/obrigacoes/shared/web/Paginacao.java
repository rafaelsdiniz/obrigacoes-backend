package br.com.obrigacoes.shared.web;

import br.com.obrigacoes.shared.exception.ParametroInvalidoException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public final class Paginacao {

    private static final String CAMPO_DESEMPATE = "id";

    private Paginacao() {
    }

    public static Pageable validar(Pageable paginacao, List<String> camposPermitidos) {
        for (Sort.Order ordem : paginacao.getSort()) {
            if (!camposPermitidos.contains(ordem.getProperty())) {
                throw new ParametroInvalidoException("Não é possível ordenar pelo campo '%s'. Campos permitidos: %s."
                        .formatted(ordem.getProperty(), String.join(", ", camposPermitidos)));
            }
        }
        if (paginacao.getOffset() > Integer.MAX_VALUE) {
            throw new ParametroInvalidoException("O número da página informado é muito alto.");
        }
        Sort ordenacao = paginacao.getSort().and(Sort.by(Sort.Direction.ASC, CAMPO_DESEMPATE));
        return PageRequest.of(paginacao.getPageNumber(), paginacao.getPageSize(), ordenacao);
    }
}
