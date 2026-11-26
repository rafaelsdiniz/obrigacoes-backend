package br.com.obrigacoes.anexo;

import br.com.obrigacoes.anexo.LimpezaDeArquivos.AnexoExcluido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LimpezaDeArquivosTest {

    @Mock
    private ArmazenamentoArquivos armazenamento;

    @InjectMocks
    private LimpezaDeArquivos limpeza;

    @Test
    void deveRemoverOArquivoDoAnexoExcluido() {
        limpeza.aoExcluirAnexo(new AnexoExcluido("1/abc"));

        verify(armazenamento).remover("1/abc");
    }

    @Test
    void naoDevePropagarFalhaNaRemocao() {
        doThrow(new UncheckedIOException(new IOException("disco indisponível"))).when(armazenamento).remover("1/abc");

        assertThatCode(() -> limpeza.aoExcluirAnexo(new AnexoExcluido("1/abc"))).doesNotThrowAnyException();
    }
}
