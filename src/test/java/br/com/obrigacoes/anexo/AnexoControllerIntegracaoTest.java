package br.com.obrigacoes.anexo;

import br.com.obrigacoes.IntegracaoTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnexoControllerIntegracaoTest extends IntegracaoTest {

    @Autowired
    private AnexosProperties propriedades;

    private long empresaId;
    private long responsavelId;
    private long obrigacaoId;

    @BeforeEach
    void cadastrarObrigacao() throws Exception {
        FileSystemUtils.deleteRecursively(propriedades.diretorio());
        empresaId = idDe(mockMvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"razaoSocial\": \"Contábil Alfa LTDA\", \"cnpj\": \"11222333000181\"}")));
        responsavelId = idDe(mockMvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Ana Souza\", \"email\": \"ana@escritorio.com.br\"}")));
        obrigacaoId = criarObrigacao("DCTFWeb");
    }

    @Test
    void deveAnexarListarEBaixarUmArquivo() throws Exception {
        byte[] pdf = AmostrasDeArquivo.pdf();

        var resposta = anexar(obrigacaoId, "relatório outubro.pdf", pdf)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeOriginal").value("relatório outubro.pdf"))
                .andExpect(jsonPath("$.tipo").value("PDF"))
                .andExpect(jsonPath("$.tipoConteudo").value("application/pdf"))
                .andExpect(jsonPath("$.tamanho").value(pdf.length))
                .andExpect(jsonPath("$.hashSha256").value(sha256(pdf)))
                .andReturn().getResponse();
        long anexoId = JsonPath.<Number>read(resposta.getContentAsString(), "$.id").longValue();
        assertThat(resposta.getHeader("Location")).endsWith(urlAnexos(obrigacaoId) + "/" + anexoId);

        mockMvc.perform(get(urlAnexos(obrigacaoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(anexoId));

        mockMvc.perform(get(urlAnexos(obrigacaoId) + "/" + anexoId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Length", String.valueOf(pdf.length)))
                .andExpect(header().string("Content-Disposition", allOf(startsWith("attachment;"),
                        containsString("filename*=UTF-8''relat%C3%B3rio%20outubro.pdf"))))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Security-Policy", "sandbox"))
                .andExpect(content().bytes(pdf));
    }

    @Test
    void deveListarDoMaisRecenteAoMaisAntigo() throws Exception {
        anexar(obrigacaoId, "guia.pdf", AmostrasDeArquivo.pdf()).andExpect(status().isCreated());
        anexar(obrigacaoId, "nota.xml", AmostrasDeArquivo.xml()).andExpect(status().isCreated());
        anexar(obrigacaoId, "folha.xlsx", AmostrasDeArquivo.xlsx()).andExpect(status().isCreated());

        mockMvc.perform(get(urlAnexos(obrigacaoId)))
                .andExpect(jsonPath("$[*].nomeOriginal").value(contains("folha.xlsx", "nota.xml", "guia.pdf")));
    }

    @Test
    void deveManterOsDocumentosAposAConclusaoEImpedirSuaExclusao() throws Exception {
        long anexoId = idDe(anexar(obrigacaoId, "guia.pdf", AmostrasDeArquivo.pdf()));
        alterarStatus("EM_ANDAMENTO");
        alterarStatus("CONCLUIDA");

        anexar(obrigacaoId, "recibo.pdf", AmostrasDeArquivo.pdf()).andExpect(status().isCreated());
        mockMvc.perform(delete(urlAnexos(obrigacaoId) + "/" + anexoId))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("REGRA_NEGOCIO"))
                .andExpect(jsonPath("$.detail")
                        .value("A obrigação está concluída e seus anexos não podem mais ser excluídos."));

        mockMvc.perform(get(urlAnexos(obrigacaoId))).andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get(urlAnexos(obrigacaoId) + "/" + anexoId))
                .andExpect(status().isOk())
                .andExpect(content().bytes(AmostrasDeArquivo.pdf()));
        assertThat(arquivosArmazenados()).isEqualTo(2);
    }

    @Test
    void deveExcluirOAnexoERemoverOArquivoDoArmazenamento() throws Exception {
        long anexoId = idDe(anexar(obrigacaoId, "guia.pdf", AmostrasDeArquivo.pdf()));
        assertThat(arquivosArmazenados()).isEqualTo(1);

        mockMvc.perform(delete(urlAnexos(obrigacaoId) + "/" + anexoId)).andExpect(status().isNoContent());

        assertThat(arquivosArmazenados()).isZero();
        mockMvc.perform(get(urlAnexos(obrigacaoId) + "/" + anexoId)).andExpect(status().isNotFound());
        mockMvc.perform(delete(urlAnexos(obrigacaoId) + "/" + anexoId)).andExpect(status().isNotFound());
    }

    static Stream<Arguments> arquivosInvalidos() {
        return Stream.of(
                Arguments.of("nota.pdf", AmostrasDeArquivo.executavel(),
                        "O conteúdo do arquivo não corresponde a um PDF válido."),
                Arguments.of("script.sh", "echo oi".getBytes(StandardCharsets.UTF_8),
                        "Tipo de arquivo não permitido. Envie um arquivo PDF, PNG, JPEG, XML, CSV, XLSX ou DOCX."),
                Arguments.of("vazio.pdf", new byte[0], "O arquivo é obrigatório e não pode estar vazio.")
        );
    }

    @ParameterizedTest
    @MethodSource("arquivosInvalidos")
    void deveRecusarArquivoInvalidoSemGravarNada(String nome, byte[] conteudo, String mensagem) throws Exception {
        anexar(obrigacaoId, nome, conteudo)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ARQUIVO_INVALIDO"))
                .andExpect(jsonPath("$.detail").value(mensagem));

        assertThat(arquivosArmazenados()).isZero();
        mockMvc.perform(get(urlAnexos(obrigacaoId))).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deveRecusarArquivoAcimaDoTamanhoMaximo() throws Exception {
        byte[] grande = new byte[(int) propriedades.tamanhoMaximo().toBytes() + 1];
        System.arraycopy(AmostrasDeArquivo.pdf(), 0, grande, 0, AmostrasDeArquivo.pdf().length);

        anexar(obrigacaoId, "grande.pdf", grande)
                .andExpect(status().isContentTooLarge())
                .andExpect(jsonPath("$.codigo").value("ARQUIVO_INVALIDO"))
                .andExpect(jsonPath("$.detail").value("O arquivo excede o tamanho máximo de %d MB."
                        .formatted(propriedades.tamanhoMaximo().toMegabytes())));
    }

    @Test
    void deveExigirOArquivoNaRequisicao() throws Exception {
        mockMvc.perform(multipart(urlAnexos(obrigacaoId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.detail").value("O parâmetro obrigatório 'arquivo' não foi informado."));
    }

    @Test
    void deveRecusarMaisDeUmArquivoPorRequisicaoSemGravarNada() throws Exception {
        mockMvc.perform(multipart(urlAnexos(obrigacaoId))
                        .file(new MockMultipartFile("arquivo", "um.pdf", "application/pdf", AmostrasDeArquivo.pdf()))
                        .file(new MockMultipartFile("arquivo", "dois.pdf", "application/pdf", AmostrasDeArquivo.pdf())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.detail").value("Envie um único arquivo por requisição."));

        mockMvc.perform(get(urlAnexos(obrigacaoId))).andExpect(jsonPath("$", hasSize(0)));
        assertThat(arquivosArmazenados()).isZero();
    }

    @Test
    void deveRecusarPlanilhaCorrompidaComoArquivoInvalido() throws Exception {
        anexar(obrigacaoId, "planilha.xlsx", AmostrasDeArquivo.zipComNomeDeEntradaInvalidoEmUtf8())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ARQUIVO_INVALIDO"));
    }

    @Test
    void deveInformarArquivoIndisponivelQuandoOConteudoSumiuDoArmazenamento() throws Exception {
        long anexoId = idDe(anexar(obrigacaoId, "guia.pdf", AmostrasDeArquivo.pdf()));
        FileSystemUtils.deleteRecursively(propriedades.diretorio());

        mockMvc.perform(get(urlAnexos(obrigacaoId) + "/" + anexoId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Arquivo indisponível"))
                .andExpect(jsonPath("$.detail").value("O arquivo deste anexo não está disponível no armazenamento."));
    }

    @Test
    void naoDeveExporAnexoPorOutraObrigacao() throws Exception {
        long anexoId = idDe(anexar(obrigacaoId, "guia.pdf", AmostrasDeArquivo.pdf()));
        long outraObrigacao = criarObrigacao("FGTS Digital");

        mockMvc.perform(get(urlAnexos(outraObrigacao) + "/" + anexoId)).andExpect(status().isNotFound());
        mockMvc.perform(delete(urlAnexos(outraObrigacao) + "/" + anexoId)).andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404ParaObrigacaoInexistente() throws Exception {
        anexar(999, "guia.pdf", AmostrasDeArquivo.pdf()).andExpect(status().isNotFound());
        mockMvc.perform(get(urlAnexos(999))).andExpect(status().isNotFound());
        mockMvc.perform(get(urlAnexos(999) + "/1")).andExpect(status().isNotFound());
        mockMvc.perform(delete(urlAnexos(999) + "/1")).andExpect(status().isNotFound());
        assertThat(arquivosArmazenados()).isZero();
    }

    private long criarObrigacao(String titulo) throws Exception {
        return idDe(mockMvc.perform(post("/api/v1/obrigacoes").contentType(MediaType.APPLICATION_JSON).content("""
                {"titulo": "%s", "tipo": "PREVIDENCIARIA", "dataVencimento": "%s", "empresaId": %d, "responsavelId": %d}
                """.formatted(titulo, LocalDate.now().plusDays(5), empresaId, responsavelId))));
    }

    private ResultActions anexar(long obrigacao, String nome, byte[] conteudo) throws Exception {
        return mockMvc.perform(multipart(urlAnexos(obrigacao))
                .file(new MockMultipartFile("arquivo", nome, "application/octet-stream", conteudo)));
    }

    private void alterarStatus(String novoStatus) throws Exception {
        mockMvc.perform(patch("/api/v1/obrigacoes/" + obrigacaoId + "/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"%s\"}".formatted(novoStatus)))
                .andExpect(status().isOk());
    }

    private long arquivosArmazenados() throws IOException {
        Path diretorio = propriedades.diretorio();
        if (!Files.exists(diretorio)) {
            return 0;
        }
        try (var arquivos = Files.walk(diretorio)) {
            return arquivos.filter(Files::isRegularFile).count();
        }
    }

    private static String urlAnexos(long obrigacao) {
        return "/api/v1/obrigacoes/" + obrigacao + "/anexos";
    }

    private static long idDe(ResultActions resultado) throws Exception {
        String corpo = resultado.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.<Number>read(corpo, "$.id").longValue();
    }

    private static String sha256(byte[] conteudo) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(conteudo));
    }
}
