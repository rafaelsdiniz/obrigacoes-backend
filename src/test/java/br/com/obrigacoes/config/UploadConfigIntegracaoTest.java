package br.com.obrigacoes.config;

import br.com.obrigacoes.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.anexos.tamanho-maximo=1KB", "app.anexos.diretorio=target/anexos-teste"})
@Import(TestcontainersConfiguration.class)
class UploadConfigIntegracaoTest {

    private static final String DELIMITADOR = "limite-do-multipart";
    private static final DataSize ACIMA_DO_LIMITE = DataSize.ofKilobytes(64);

    @Value("${local.server.port}")
    private int porta;

    @Test
    void deveAplicarOLimiteDosAnexosAoMultipartDoServidor() throws Exception {
        URI anexos = URI.create("http://localhost:%d/api/v1/obrigacoes/1/anexos".formatted(porta));
        HttpRequest requisicao = HttpRequest.newBuilder(anexos)
                .header("Content-Type", "multipart/form-data; boundary=" + DELIMITADOR)
                .POST(HttpRequest.BodyPublishers.ofByteArray(
                        multipart(new byte[(int) ACIMA_DO_LIMITE.toBytes()])))
                .build();

        HttpResponse<String> resposta;
        try (HttpClient cliente = HttpClient.newHttpClient()) {
            resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
        }

        assertThat(resposta.statusCode()).isEqualTo(413);
        assertThat(resposta.body())
                .contains("\"codigo\":\"ARQUIVO_INVALIDO\"")
                .contains("O arquivo excede o tamanho máximo de 1 KB.");
    }

    private static byte[] multipart(byte[] conteudo) {
        ByteArrayOutputStream corpo = new ByteArrayOutputStream();
        corpo.writeBytes(("--" + DELIMITADOR + "\r\n"
                + "Content-Disposition: form-data; name=\"arquivo\"; filename=\"grande.pdf\"\r\n"
                + "Content-Type: application/pdf\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        corpo.writeBytes(conteudo);
        corpo.writeBytes(("\r\n--" + DELIMITADOR + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return corpo.toByteArray();
    }
}
