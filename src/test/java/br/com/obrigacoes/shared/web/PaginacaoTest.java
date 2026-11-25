package br.com.obrigacoes.shared.web;

import br.com.obrigacoes.shared.exception.ParametroInvalidoException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginacaoTest {

    private static final List<String> PERMITIDOS = List.of("nome", "criadoEm");

    @Test
    void deveManterOrdenacaoPermitidaEAcrescentarIdComoDesempate() {
        Pageable paginacao = PageRequest.of(2, 20, Sort.by("nome").and(Sort.by(Sort.Direction.DESC, "criadoEm")));

        Pageable resultado = Paginacao.validar(paginacao, PERMITIDOS);

        assertThat(resultado.getPageNumber()).isEqualTo(2);
        assertThat(resultado.getPageSize()).isEqualTo(20);
        assertThat(resultado.getSort())
                .containsExactly(Sort.Order.asc("nome"), Sort.Order.desc("criadoEm"), Sort.Order.asc("id"));
    }

    @Test
    void deveOrdenarPorIdQuandoNaoHouverOrdenacao() {
        assertThat(Paginacao.validar(PageRequest.of(0, 20), PERMITIDOS).getSort())
                .containsExactly(Sort.Order.asc("id"));
    }

    @Test
    void deveRejeitarCampoNaoPermitidoOuExpressaoArbitraria() {
        assertThatThrownBy(() -> Paginacao.validar(PageRequest.of(0, 20, Sort.by("versao")), PERMITIDOS))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Não é possível ordenar pelo campo 'versao'. Campos permitidos: nome, criadoEm.");

        assertThatThrownBy(() -> Paginacao.validar(PageRequest.of(0, 20, Sort.by("nome;DROP TABLE usuario")), PERMITIDOS))
                .isInstanceOf(ParametroInvalidoException.class);
    }

    @Test
    void deveRejeitarPaginaCujoDeslocamentoExcedeOLimiteDoBanco() {
        assertThatThrownBy(() -> Paginacao.validar(PageRequest.of(Integer.MAX_VALUE, 100), PERMITIDOS))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("O número da página informado é muito alto.");
    }
}
