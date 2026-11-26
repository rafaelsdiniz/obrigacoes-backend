package br.com.obrigacoes.obrigacao;

import br.com.obrigacoes.empresa.Empresa;
import br.com.obrigacoes.shared.exception.RegraNegocioException;
import br.com.obrigacoes.usuario.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

import static br.com.obrigacoes.obrigacao.StatusObrigacao.CONCLUIDA;
import static br.com.obrigacoes.obrigacao.StatusObrigacao.EM_ANDAMENTO;
import static br.com.obrigacoes.obrigacao.StatusObrigacao.PENDENTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class ObrigacaoTest {

    private static final Instant AGORA = Instant.parse("2026-11-10T13:00:00Z");
    private static final LocalDate VENCIMENTO = LocalDate.of(2026, 11, 20);

    private final Empresa empresa = new Empresa("Contábil Alfa LTDA", null, "11222333000181", null, null);
    private final Usuario responsavel = new Usuario("Ana Souza", "ana@escritorio.com.br", null);

    @Test
    void deveIniciarPendenteERegistrarACriacaoNoHistorico() {
        Obrigacao obrigacao = nova();

        assertThat(obrigacao.getStatus()).isEqualTo(PENDENTE);
        assertThat(obrigacao.getHistorico())
                .extracting(HistoricoStatusObrigacao::getStatusAnterior, HistoricoStatusObrigacao::getStatusNovo)
                .containsExactly(tuple(null, PENDENTE));
    }

    @Test
    void deveSeguirOFluxoCompletoRegistrandoCadaTransicao() {
        Obrigacao obrigacao = nova();

        obrigacao.alterarStatus(EM_ANDAMENTO, AGORA.plusSeconds(60));
        obrigacao.alterarStatus(CONCLUIDA, AGORA.plusSeconds(120));

        assertThat(obrigacao.getStatus()).isEqualTo(CONCLUIDA);
        assertThat(obrigacao.getHistorico())
                .extracting(HistoricoStatusObrigacao::getStatusAnterior, HistoricoStatusObrigacao::getStatusNovo)
                .containsExactly(tuple(null, PENDENTE), tuple(PENDENTE, EM_ANDAMENTO), tuple(EM_ANDAMENTO, CONCLUIDA));
    }

    @Test
    void naoDevePularEtapas() {
        Obrigacao obrigacao = nova();

        assertThatThrownBy(() -> obrigacao.alterarStatus(CONCLUIDA, AGORA))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é possível alterar o status de Pendente para Concluída. O próximo status permitido é Em andamento.");
        assertThat(obrigacao.getStatus()).isEqualTo(PENDENTE);
        assertThat(obrigacao.getHistorico()).hasSize(1);
    }

    @Test
    void naoDeveRetrocederStatus() {
        Obrigacao obrigacao = nova();
        obrigacao.alterarStatus(EM_ANDAMENTO, AGORA);

        assertThatThrownBy(() -> obrigacao.alterarStatus(PENDENTE, AGORA))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(obrigacao.getStatus()).isEqualTo(EM_ANDAMENTO);
        assertThat(obrigacao.getHistorico()).hasSize(2);
    }

    @Test
    void naoDeveAceitarOMesmoStatus() {
        Obrigacao obrigacao = nova();

        assertThatThrownBy(() -> obrigacao.alterarStatus(PENDENTE, AGORA))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A obrigação já está com o status Pendente.");
    }

    @Test
    void obrigacaoConcluidaNaoPodeTerStatusNemDadosAlterados() {
        Obrigacao obrigacao = nova();
        obrigacao.alterarStatus(EM_ANDAMENTO, AGORA);
        obrigacao.alterarStatus(CONCLUIDA, AGORA);

        assertThatThrownBy(() -> obrigacao.alterarStatus(EM_ANDAMENTO, AGORA))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A obrigação já foi concluída e não pode mais ser alterada.");
        assertThatThrownBy(() -> obrigacao.atualizarDados("Outro título", null, TipoObrigacao.FISCAL, null,
                VENCIMENTO, empresa, responsavel))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(obrigacao.getTitulo()).isEqualTo("Entrega da DCTFWeb");
    }

    @Test
    void deveEstarAtrasadaSomenteSeVencidaENaoConcluida() {
        Obrigacao obrigacao = nova();

        assertThat(obrigacao.isAtrasada(VENCIMENTO)).isFalse();
        assertThat(obrigacao.isAtrasada(VENCIMENTO.plusDays(1))).isTrue();

        obrigacao.alterarStatus(EM_ANDAMENTO, AGORA);
        assertThat(obrigacao.isAtrasada(VENCIMENTO.plusDays(1))).isTrue();

        obrigacao.alterarStatus(CONCLUIDA, AGORA);
        assertThat(obrigacao.isAtrasada(VENCIMENTO.plusDays(1))).isFalse();
    }

    private Obrigacao nova() {
        return new Obrigacao("  Entrega da DCTFWeb ", " ", TipoObrigacao.PREVIDENCIARIA, YearMonth.of(2026, 10),
                VENCIMENTO, empresa, responsavel, AGORA);
    }
}
