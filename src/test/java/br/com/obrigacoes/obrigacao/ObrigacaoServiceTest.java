package br.com.obrigacoes.obrigacao;

import br.com.obrigacoes.empresa.Empresa;
import br.com.obrigacoes.empresa.EmpresaService;
import br.com.obrigacoes.obrigacao.dto.ObrigacaoRequest;
import br.com.obrigacoes.shared.exception.RegraNegocioException;
import br.com.obrigacoes.usuario.Usuario;
import br.com.obrigacoes.usuario.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObrigacaoServiceTest {

    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-10T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @Mock
    private ObrigacaoRepository repository;
    @Mock
    private HistoricoStatusObrigacaoRepository historicoRepository;
    @Mock
    private EmpresaService empresaService;
    @Mock
    private UsuarioService usuarioService;

    private ObrigacaoService service;

    private final Empresa empresa = comId(new Empresa("Contábil Alfa LTDA", null, "11222333000181", null, null), 1L);
    private final Usuario responsavel = comId(new Usuario("Ana Souza", "ana@escritorio.com.br", null), 10L);

    @BeforeEach
    void setUp() {
        service = new ObrigacaoService(repository, historicoRepository, empresaService, usuarioService, RELOGIO);
    }

    @Test
    void deveCriarObrigacaoUsandoApenasEmpresaEResponsavelAtivos() {
        when(empresaService.buscarAtivaPorId(1L)).thenReturn(empresa);
        when(usuarioService.buscarAtivoPorId(10L)).thenReturn(responsavel);
        when(repository.save(any(Obrigacao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Obrigacao obrigacao = service.criar(requisicao(1L, 10L, null));

        assertThat(obrigacao.getStatus()).isEqualTo(StatusObrigacao.PENDENTE);
        assertThat(obrigacao.getHistorico()).singleElement()
                .extracting(HistoricoStatusObrigacao::getAlteradoEm)
                .isEqualTo(Instant.parse("2026-11-10T13:00:00Z"));
    }

    @Test
    void naoDeveCriarObrigacaoParaEmpresaInativa() {
        when(empresaService.buscarAtivaPorId(1L)).thenThrow(new RegraNegocioException("A empresa está inativa."));

        assertThatThrownBy(() -> service.criar(requisicao(1L, 10L, null)))
                .isInstanceOf(RegraNegocioException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void devePermitirEditarMantendoResponsavelQueFoiInativadoDepois() {
        Obrigacao obrigacao = existente();
        responsavel.inativar();
        when(repository.findById(5L)).thenReturn(Optional.of(obrigacao));

        Obrigacao atualizada = service.atualizar(5L, requisicao(1L, 10L, obrigacao.getVersao()));

        assertThat(atualizada.getTitulo()).isEqualTo("Entrega da DCTFWeb");
        verify(empresaService, never()).buscarAtivaPorId(anyLong());
        verify(usuarioService, never()).buscarAtivoPorId(anyLong());
    }

    @Test
    void deveExigirResponsavelAtivoAoTrocarOResponsavel() {
        Obrigacao obrigacao = existente();
        when(repository.findById(5L)).thenReturn(Optional.of(obrigacao));
        when(usuarioService.buscarAtivoPorId(20L)).thenThrow(new RegraNegocioException("O usuário está inativo."));

        assertThatThrownBy(() -> service.atualizar(5L, requisicao(1L, 20L, obrigacao.getVersao())))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(obrigacao.getResponsavel()).isSameAs(responsavel);
    }

    @Test
    void deveRegistrarATransicaoComOHorarioDoRelogio() {
        Obrigacao obrigacao = existente();
        when(repository.buscarComBloqueio(5L)).thenReturn(Optional.of(obrigacao));

        service.alterarStatus(5L, StatusObrigacao.EM_ANDAMENTO);

        assertThat(obrigacao.getHistorico()).last()
                .extracting(HistoricoStatusObrigacao::getStatusNovo, HistoricoStatusObrigacao::getAlteradoEm)
                .containsExactly(StatusObrigacao.EM_ANDAMENTO, Instant.parse("2026-11-10T13:00:00Z"));
    }

    private Obrigacao existente() {
        return new Obrigacao("Entrega da DCTFWeb", null, TipoObrigacao.PREVIDENCIARIA, null,
                LocalDate.of(2026, 11, 20), empresa, responsavel, Instant.now(RELOGIO));
    }

    private static ObrigacaoRequest requisicao(Long empresaId, Long responsavelId, Long versao) {
        return new ObrigacaoRequest("Entrega da DCTFWeb", null, TipoObrigacao.PREVIDENCIARIA, null,
                LocalDate.of(2026, 11, 20), empresaId, responsavelId, versao);
    }

    private static <T> T comId(T entidade, Long id) {
        ReflectionTestUtils.setField(entidade, "id", id);
        return entidade;
    }
}
