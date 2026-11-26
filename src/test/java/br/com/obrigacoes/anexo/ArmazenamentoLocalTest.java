package br.com.obrigacoes.anexo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArmazenamentoLocalTest {

    private static final String SHA256_DE_ABC = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

    @TempDir
    private Path raiz;

    private ArmazenamentoLocal armazenamento;

    @BeforeEach
    void setUp() {
        armazenamento = new ArmazenamentoLocal(new AnexosProperties(raiz, DataSize.ofMegabytes(1)));
    }

    @Test
    void deveGravarOConteudoNaPastaInformadaComTamanhoEHash() throws IOException {
        ArquivoArmazenado armazenado = armazenamento.salvar("42", conteudo("abc"));

        assertThat(armazenado.chave()).matches("42/[0-9a-f-]{36}");
        assertThat(armazenado.tamanho()).isEqualTo(3);
        assertThat(armazenado.hashSha256()).isEqualTo(SHA256_DE_ABC);
        assertThat(raiz.resolve(armazenado.chave())).hasContent("abc");
    }

    @Test
    void deveGerarUmaChaveDiferenteParaCadaArquivo() {
        assertThat(armazenamento.salvar("42", conteudo("abc")).chave())
                .isNotEqualTo(armazenamento.salvar("42", conteudo("abc")).chave());
    }

    @Test
    void deveAbrirERemoverUmArquivoGravado() throws IOException {
        String chave = armazenamento.salvar("7", conteudo("abc")).chave();

        Resource recurso = armazenamento.abrir(chave);
        assertThat(recurso.getContentAsString(StandardCharsets.UTF_8)).isEqualTo("abc");

        armazenamento.remover(chave);
        assertThat(raiz.resolve(chave)).doesNotExist();
        armazenamento.remover(chave);
    }

    @Test
    void deveFalharAoAbrirArquivoAusente() {
        assertThatThrownBy(() -> armazenamento.abrir("7/inexistente"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void naoDevePermitirSairDoDiretorioDeAnexos() {
        assertThatThrownBy(() -> armazenamento.salvar("../fora", conteudo("abc")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> armazenamento.abrir("../../etc/passwd"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> armazenamento.remover("."))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void naoDeveDeixarArquivoParcialQuandoALeituraFalha() throws IOException {
        InputStream interrompido = new InputStream() {
            private int lidos;

            @Override
            public int read() throws IOException {
                if (lidos++ < 10) {
                    return 'x';
                }
                throw new IOException("conexão interrompida");
            }
        };

        assertThatThrownBy(() -> armazenamento.salvar("9", interrompido))
                .isInstanceOf(UncheckedIOException.class);
        try (var arquivos = Files.walk(raiz)) {
            assertThat(arquivos.filter(Files::isRegularFile)).isEmpty();
        }
    }

    private static InputStream conteudo(String texto) {
        return new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8));
    }
}
