package br.com.obrigacoes.anexo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class NomesDeArquivoTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "recibo.pdf|recibo.pdf",
            "../../etc/passwd.pdf|passwd.pdf",
            "C:\\Users\\ana\\Documentos\\guia.pdf|guia.pdf",
            "  relatório final.xlsx  |relatório final.xlsx"
    })
    void deveManterApenasONomeDoArquivoSemCaminho(String enviado, String esperado) {
        assertThat(NomesDeArquivo.sanitizar(enviado)).isEqualTo(esperado);
    }

    @Test
    void deveRemoverCaracteresDeControle() {
        assertThat(NomesDeArquivo.sanitizar("guia\r\nX-Injetado: 1\u0000.pdf")).isEqualTo("guiaX-Injetado: 1.pdf");
    }

    @Test
    void deveRemoverCaracteresInvisiveisQueDisfarcamAExtensao() {
        assertThat(NomesDeArquivo.sanitizar("fatura\u202Efdp.exe\u0085.pdf")).isEqualTo("faturafdp.exe.pdf");
    }

    @Test
    void deveTratarNomeAusenteComoVazio() {
        assertThat(NomesDeArquivo.sanitizar(null)).isEmpty();
    }

    @Test
    void deveTruncarNomesLongosPreservandoAExtensao() {
        String nome = NomesDeArquivo.sanitizar("a".repeat(300) + ".pdf");

        assertThat(nome).hasSize(NomesDeArquivo.TAMANHO_MAXIMO).endsWith("a.pdf");
    }

    @Test
    void deveTruncarONomeInteiroQuandoAExtensaoNaoCabe() {
        assertThat(NomesDeArquivo.sanitizar("x." + "a".repeat(300))).isEqualTo("x." + "a".repeat(253));
        assertThat(NomesDeArquivo.sanitizar("x".repeat(10) + "." + "a".repeat(254)))
                .hasSize(NomesDeArquivo.TAMANHO_MAXIMO);
    }

    @Test
    void naoDeveCortarCaractereFormadoPorParSubstituto() {
        String nome = NomesDeArquivo.sanitizar("a".repeat(250) + "😀😀😀.pdf");

        assertThat(nome).isEqualTo("a".repeat(250) + ".pdf");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "guia.PDF|pdf",
            "planilha.final.xlsx|xlsx",
            ".csv|csv",
            "sem-extensao|''",
            "termina-com-ponto.|''"
    })
    void deveExtrairAExtensaoEmMinusculas(String nome, String esperada) {
        assertThat(NomesDeArquivo.extensao(nome)).isEqualTo(esperada);
    }
}
