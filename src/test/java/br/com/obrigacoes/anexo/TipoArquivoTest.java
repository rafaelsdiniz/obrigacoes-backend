package br.com.obrigacoes.anexo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.unit.DataSize;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

import static br.com.obrigacoes.anexo.AmostrasDeArquivo.bytes;
import static org.assertj.core.api.Assertions.assertThat;

class TipoArquivoTest {

    static Stream<Arguments> conteudosValidos() {
        return Stream.of(
                Arguments.of(TipoArquivo.PDF, AmostrasDeArquivo.pdf()),
                Arguments.of(TipoArquivo.PNG, AmostrasDeArquivo.png()),
                Arguments.of(TipoArquivo.JPEG, AmostrasDeArquivo.jpeg()),
                Arguments.of(TipoArquivo.XML, AmostrasDeArquivo.xml()),
                Arguments.of(TipoArquivo.XML, "  \n<raiz/>".getBytes(StandardCharsets.UTF_8)),
                Arguments.of(TipoArquivo.CSV, AmostrasDeArquivo.csv()),
                Arguments.of(TipoArquivo.CSV, "razão;cnpj\n".getBytes(StandardCharsets.ISO_8859_1)),
                Arguments.of(TipoArquivo.XLSX, AmostrasDeArquivo.xlsx()),
                Arguments.of(TipoArquivo.DOCX, AmostrasDeArquivo.docx())
        );
    }

    static Stream<Arguments> conteudosQueNaoCorrespondemAoTipo() {
        return Stream.of(
                Arguments.of(TipoArquivo.PDF, AmostrasDeArquivo.executavel()),
                Arguments.of(TipoArquivo.PDF, "%PD".getBytes(StandardCharsets.US_ASCII)),
                Arguments.of(TipoArquivo.PNG, AmostrasDeArquivo.jpeg()),
                Arguments.of(TipoArquivo.JPEG, AmostrasDeArquivo.png()),
                Arguments.of(TipoArquivo.XML, "nome;valor".getBytes(StandardCharsets.UTF_8)),
                Arguments.of(TipoArquivo.XML, bytes('<', 'a', 0x00, '>')),
                Arguments.of(TipoArquivo.CSV, AmostrasDeArquivo.executavel()),
                Arguments.of(TipoArquivo.CSV, bytes('a', ';', 0x07, 'b')),
                Arguments.of(TipoArquivo.XLSX, AmostrasDeArquivo.docx()),
                Arguments.of(TipoArquivo.DOCX, AmostrasDeArquivo.xlsx()),
                Arguments.of(TipoArquivo.DOCX, AmostrasDeArquivo.zip("word")),
                Arguments.of(TipoArquivo.XLSX, AmostrasDeArquivo.pdf())
        );
    }

    @ParameterizedTest
    @MethodSource("conteudosValidos")
    void deveReconhecerOConteudoDeCadaTipoPermitido(TipoArquivo tipo, byte[] conteudo) throws Exception {
        assertThat(tipo.reconhece(new ByteArrayResource(conteudo))).isTrue();
    }

    @ParameterizedTest
    @MethodSource("conteudosQueNaoCorrespondemAoTipo")
    void naoDeveReconhecerConteudoDeOutroTipo(TipoArquivo tipo, byte[] conteudo) throws Exception {
        assertThat(tipo.reconhece(new ByteArrayResource(conteudo))).isFalse();
    }

    @Test
    void naoDeveReconhecerPlanilhaTruncadaNemZipCorrompido() throws Exception {
        byte[] truncado = Arrays.copyOf(AmostrasDeArquivo.xlsx(), 40);

        assertThat(TipoArquivo.XLSX.reconhece(new ByteArrayResource(truncado))).isFalse();
        assertThat(TipoArquivo.DOCX.reconhece(new ByteArrayResource(new byte[]{'P', 'K', 3, 4, 0}))).isFalse();
    }

    @Test
    void naoDeveFalharComZipCujoNomeDeEntradaNaoEhUtf8Valido() throws Exception {
        byte[] corrompido = AmostrasDeArquivo.zipComNomeDeEntradaInvalidoEmUtf8();

        assertThat(TipoArquivo.XLSX.reconhece(new ByteArrayResource(corrompido))).isFalse();
        assertThat(TipoArquivo.DOCX.reconhece(new ByteArrayResource(corrompido))).isFalse();
    }

    @Test
    void deveDesistirDeInspecionarZipQueDescompactaAlemDoOrcamento() throws Exception {
        byte[] bomba = AmostrasDeArquivo.zipComPrimeiraEntradaDe(DataSize.ofMegabytes(17), "xl/workbook.xml");

        assertThat(TipoArquivo.XLSX.reconhece(new ByteArrayResource(bomba))).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"pdf,PDF", "png,PNG", "jpg,JPEG", "jpeg,JPEG", "xml,XML", "csv,CSV", "xlsx,XLSX", "docx,DOCX"})
    void deveIdentificarOTipoPelaExtensao(String extensao, TipoArquivo esperado) {
        assertThat(TipoArquivo.daExtensao(extensao)).contains(esperado);
    }

    @ParameterizedTest
    @CsvSource({"exe", "zip", "html", "''"})
    void naoDeveAceitarExtensoesForaDaLista(String extensao) {
        assertThat(TipoArquivo.daExtensao(extensao)).isEmpty();
    }

    @Test
    void deveDescreverOsTiposPermitidos() {
        assertThat(TipoArquivo.descricaoDosPermitidos()).isEqualTo("PDF, PNG, JPEG, XML, CSV, XLSX ou DOCX");
    }
}
