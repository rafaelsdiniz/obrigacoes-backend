package br.com.obrigacoes.anexo;

import br.com.obrigacoes.anexo.ValidadorArquivo.ArquivoValidado;
import br.com.obrigacoes.shared.exception.ArquivoInvalidoException;
import br.com.obrigacoes.shared.exception.ArquivoMuitoGrandeException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidadorArquivoTest {

    private final ValidadorArquivo validador =
            new ValidadorArquivo(new AnexosProperties(Path.of("anexos"), DataSize.ofKilobytes(1)));

    @Test
    void deveAceitarArquivoPermitidoComNomeSanitizado() {
        ArquivoValidado validado = validador.validar(arquivo("../recibo DCTFWeb.PDF", AmostrasDeArquivo.pdf()));

        assertThat(validado.nome()).isEqualTo("recibo DCTFWeb.PDF");
        assertThat(validado.tipo()).isEqualTo(TipoArquivo.PDF);
    }

    @Test
    void deveRecusarArquivoAusenteOuVazio() {
        assertThatThrownBy(() -> validador.validar(null))
                .isInstanceOf(ArquivoInvalidoException.class)
                .hasMessage("O arquivo é obrigatório e não pode estar vazio.");
        assertThatThrownBy(() -> validador.validar(arquivo("recibo.pdf", new byte[0])))
                .isInstanceOf(ArquivoInvalidoException.class);
    }

    @Test
    void deveRecusarArquivoAcimaDoTamanhoMaximo() {
        byte[] grande = new byte[1025];
        System.arraycopy(AmostrasDeArquivo.pdf(), 0, grande, 0, AmostrasDeArquivo.pdf().length);

        assertThatThrownBy(() -> validador.validar(arquivo("recibo.pdf", grande)))
                .isInstanceOf(ArquivoMuitoGrandeException.class)
                .hasMessage("O arquivo excede o tamanho máximo de 1 KB.");
    }

    @Test
    void deveRecusarExtensaoNaoPermitidaOuAusente() {
        String mensagem = "Tipo de arquivo não permitido. Envie um arquivo PDF, PNG, JPEG, XML, CSV, XLSX ou DOCX.";

        assertThatThrownBy(() -> validador.validar(arquivo("instalador.exe", AmostrasDeArquivo.executavel())))
                .isInstanceOf(ArquivoInvalidoException.class)
                .hasMessage(mensagem);
        assertThatThrownBy(() -> validador.validar(arquivo("recibo", AmostrasDeArquivo.pdf())))
                .isInstanceOf(ArquivoInvalidoException.class)
                .hasMessage(mensagem);
    }

    @Test
    void deveRecusarArquivoCujoConteudoNaoCorrespondeAExtensao() {
        assertThatThrownBy(() -> validador.validar(arquivo("recibo.pdf", AmostrasDeArquivo.executavel())))
                .isInstanceOf(ArquivoInvalidoException.class)
                .hasMessage("O conteúdo do arquivo não corresponde a um PDF válido.");
    }

    private static MockMultipartFile arquivo(String nome, byte[] conteudo) {
        return new MockMultipartFile("arquivo", nome, "application/octet-stream", conteudo);
    }
}
