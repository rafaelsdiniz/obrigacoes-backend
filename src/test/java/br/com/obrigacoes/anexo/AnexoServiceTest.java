package br.com.obrigacoes.anexo;

import br.com.obrigacoes.anexo.LimpezaDeArquivos.AnexoExcluido;
import br.com.obrigacoes.anexo.ValidadorArquivo.ArquivoValidado;
import br.com.obrigacoes.empresa.Empresa;
import br.com.obrigacoes.obrigacao.Obrigacao;
import br.com.obrigacoes.obrigacao.ObrigacaoService;
import br.com.obrigacoes.obrigacao.StatusObrigacao;
import br.com.obrigacoes.obrigacao.TipoObrigacao;
import br.com.obrigacoes.shared.exception.ArquivoInvalidoException;
import br.com.obrigacoes.shared.exception.RecursoNaoEncontradoException;
import br.com.obrigacoes.shared.exception.RegraNegocioException;
import br.com.obrigacoes.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionOperations;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnexoServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-10T13:00:00Z");
    private static final ArquivoArmazenado ARMAZENADO = new ArquivoArmazenado("5/chave", 42, "a".repeat(64));

    @Mock
    private AnexoRepository repository;
    @Mock
    private ObrigacaoService obrigacaoService;
    @Mock
    private ValidadorArquivo validador;
    @Mock
    private ArmazenamentoArquivos armazenamento;
    @Mock
    private ApplicationEventPublisher eventos;

    private AnexoService service;

    private final MockMultipartFile arquivo =
            new MockMultipartFile("arquivo", "recibo.pdf", "application/pdf", AmostrasDeArquivo.pdf());

    @BeforeEach
    void setUp() {
        service = new AnexoService(repository, obrigacaoService, validador, armazenamento, eventos,
                TransactionOperations.withoutTransaction());
    }

    @Test
    void deveArmazenarOArquivoERegistrarOAnexo() {
        prepararArquivoValidoArmazenado();
        when(repository.save(any(Anexo.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Anexo anexo = service.anexar(5L, arquivo);

        assertThat(anexo.getNomeOriginal()).isEqualTo("recibo.pdf");
        assertThat(anexo.getTipo()).isEqualTo(TipoArquivo.PDF);
        assertThat(anexo.getTamanho()).isEqualTo(42);
        assertThat(anexo.getChaveArmazenamento()).isEqualTo("5/chave");
        assertThat(anexo.getHashSha256()).isEqualTo("a".repeat(64));
        verify(armazenamento, never()).remover(any());
    }

    @Test
    void deveRemoverOArquivoArmazenadoQuandoORegistroFalha() {
        prepararArquivoValidoArmazenado();
        DataIntegrityViolationException falha = new DataIntegrityViolationException("falha no banco");
        when(repository.save(any(Anexo.class))).thenThrow(falha);

        assertThatThrownBy(() -> service.anexar(5L, arquivo)).isSameAs(falha);
        verify(armazenamento).remover("5/chave");
    }

    @Test
    void devePreservarAFalhaOriginalMesmoSeARemocaoTambemFalhar() {
        prepararArquivoValidoArmazenado();
        DataIntegrityViolationException falha = new DataIntegrityViolationException("falha no banco");
        when(repository.save(any(Anexo.class))).thenThrow(falha);
        IllegalStateException falhaNaRemocao = new IllegalStateException("disco indisponível");
        doThrow(falhaNaRemocao).when(armazenamento).remover("5/chave");

        assertThatThrownBy(() -> service.anexar(5L, arquivo))
                .isSameAs(falha)
                .hasSuppressedException(falhaNaRemocao);
    }

    @Test
    void naoDeveArmazenarArquivoInvalido() {
        when(validador.validar(arquivo)).thenThrow(new ArquivoInvalidoException("Tipo de arquivo não permitido."));

        assertThatThrownBy(() -> service.anexar(5L, arquivo)).isInstanceOf(ArquivoInvalidoException.class);
        verifyNoInteractions(armazenamento, repository);
    }

    @Test
    void naoDeveArmazenarArquivoDeObrigacaoInexistente() {
        doThrow(new RecursoNaoEncontradoException("obrigação", 5L)).when(obrigacaoService).verificarExistencia(5L);

        assertThatThrownBy(() -> service.anexar(5L, arquivo)).isInstanceOf(RecursoNaoEncontradoException.class);
        verifyNoInteractions(validador, armazenamento, repository);
    }

    @Test
    void deveExcluirOAnexoEAgendarARemocaoDoArquivoAposOCommit() {
        Obrigacao obrigacao = obrigacao();
        Anexo anexo = new Anexo(obrigacao, "recibo.pdf", TipoArquivo.PDF, ARMAZENADO);
        when(obrigacaoService.buscarComBloqueio(5L)).thenReturn(obrigacao);
        when(repository.findByIdAndObrigacaoId(9L, 5L)).thenReturn(Optional.of(anexo));

        service.excluir(5L, 9L);

        verify(repository).delete(anexo);
        verify(eventos).publishEvent(new AnexoExcluido("5/chave"));
    }

    @Test
    void naoDeveExcluirAnexoDeObrigacaoConcluida() {
        Obrigacao obrigacao = obrigacao();
        obrigacao.alterarStatus(StatusObrigacao.EM_ANDAMENTO, AGORA);
        obrigacao.alterarStatus(StatusObrigacao.CONCLUIDA, AGORA);
        when(obrigacaoService.buscarComBloqueio(5L)).thenReturn(obrigacao);
        when(repository.findByIdAndObrigacaoId(9L, 5L))
                .thenReturn(Optional.of(new Anexo(obrigacao, "recibo.pdf", TipoArquivo.PDF, ARMAZENADO)));

        assertThatThrownBy(() -> service.excluir(5L, 9L)).isInstanceOf(RegraNegocioException.class);
        verify(repository, never()).delete(any());
        verifyNoInteractions(eventos);
    }

    @Test
    void deveTratarAnexoDeOutraObrigacaoComoInexistente() {
        when(repository.findByIdAndObrigacaoId(9L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.baixar(5L, 9L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Não existe anexo com o id 9.");
        verifyNoInteractions(armazenamento);
    }

    private void prepararArquivoValidoArmazenado() {
        when(validador.validar(arquivo)).thenReturn(new ArquivoValidado("recibo.pdf", TipoArquivo.PDF));
        when(armazenamento.salvar(eq("5"), any(InputStream.class))).thenReturn(ARMAZENADO);
        when(obrigacaoService.referenciaPara(5L)).thenReturn(obrigacao());
    }

    private static Obrigacao obrigacao() {
        return new Obrigacao("DCTFWeb", null, TipoObrigacao.PREVIDENCIARIA, null, LocalDate.of(2026, 11, 20),
                new Empresa("Contábil Alfa LTDA", null, "11222333000181", null, null),
                new Usuario("Ana Souza", "ana@escritorio.com.br", null), AGORA);
    }
}
