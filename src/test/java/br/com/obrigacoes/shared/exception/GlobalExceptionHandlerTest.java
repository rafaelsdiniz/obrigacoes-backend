package br.com.obrigacoes.shared.exception;

import jakarta.servlet.MultipartConfigElement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
                .setControllerAdvice(new GlobalExceptionHandler(
                        new MultipartConfigElement("", DataSize.ofMegabytes(10).toBytes(), -1, 0)))
                .setValidator(validator)
                .build();
    }

    @Test
    void deveRetornar404ParaRecursoNaoEncontrado() throws Exception {
        mockMvc.perform(get("/teste/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.detail").value("Não existe empresa com o id 99."))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void deveRetornar422ParaRegraDeNegocio() throws Exception {
        mockMvc.perform(get("/teste/regra"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("REGRA_NEGOCIO"))
                .andExpect(jsonPath("$.detail").value("Empresa inativa não pode receber obrigações."));
    }

    @Test
    void deveRetornar409ParaConflito() throws Exception {
        mockMvc.perform(get("/teste/conflito"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO"));
    }

    @Test
    void deveRetornar409SemExporDetalhesDoBancoParaViolacaoDeIntegridade() throws Exception {
        mockMvc.perform(get("/teste/integridade"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO"))
                .andExpect(jsonPath("$.detail").value("A operação viola uma restrição de integridade dos dados."));
    }

    @Test
    void deveRetornar409ParaEdicaoConcorrente() throws Exception {
        mockMvc.perform(get("/teste/concorrencia"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO"));
    }

    @Test
    void deveRetornar400ParaMultipartMalformado() throws Exception {
        mockMvc.perform(get("/teste/multipart"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"));
    }

    @Test
    void deveRetornar409ParaDeadlockOuEsperaDeBloqueio() throws Exception {
        mockMvc.perform(get("/teste/bloqueio"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO"));
    }

    @Test
    void deveListarErrosPorCampoQuandoCorpoForInvalido() throws Exception {
        mockMvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                .andExpect(jsonPath("$.erros", hasSize(1)))
                .andExpect(jsonPath("$.erros[0].campo").value("nome"))
                .andExpect(jsonPath("$.erros[0].mensagem").value("O nome é obrigatório."));
    }

    @Test
    void deveValidarParametrosDoMetodo() throws Exception {
        mockMvc.perform(get("/teste/parametro").param("pagina", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                .andExpect(jsonPath("$.erros[0].campo").value("pagina"))
                .andExpect(jsonPath("$.erros[0].mensagem").value("A página deve ser positiva."));
    }

    @Test
    void deveRetornar400ParaJsonMalformado() throws Exception {
        mockMvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{nome:"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.detail").value("O corpo da requisição está ausente ou malformado."));
    }

    @Test
    void deveRetornar400ParaTipoDeParametroInvalido() throws Exception {
        mockMvc.perform(get("/teste/id/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.detail").value("O valor 'abc' é inválido para o parâmetro 'id'."));
    }

    @Test
    void deveRetornarRecursoNaoEncontradoParaRotaInexistente() throws Exception {
        mockMvc.perform(get("/rota/que/nao/existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
    }

    @Test
    void deveTraduzirErrosGeradosPeloSpring() throws Exception {
        mockMvc.perform(put("/teste/conflito"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Método não permitido"))
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void deveInformarEmPortuguesQualParametroObrigatorioFaltou() throws Exception {
        mockMvc.perform(get("/teste/parametro"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.detail").value("O parâmetro obrigatório 'pagina' não foi informado."));
    }

    @Test
    void deveInformarEmPortuguesQualParteDoMultipartFaltou() throws Exception {
        mockMvc.perform(multipart("/teste/upload"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.detail").value("O parâmetro obrigatório 'arquivo' não foi informado."));
    }

    @Test
    void deveInformarOLimiteQuandoOUploadExcedeOTamanhoMaximo() throws Exception {
        mockMvc.perform(get("/teste/upload-grande"))
                .andExpect(status().isContentTooLarge())
                .andExpect(jsonPath("$.codigo").value("ARQUIVO_INVALIDO"))
                .andExpect(jsonPath("$.detail").value("O arquivo excede o tamanho máximo de 10 MB."));
    }

    @Test
    void deveOcultarDetalhesDeErroInesperado() throws Exception {
        mockMvc.perform(get("/teste/inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERRO_INTERNO"))
                .andExpect(jsonPath("$.detail").value("Ocorreu um erro inesperado. Tente novamente mais tarde."));
    }

    record RequisicaoDeTeste(@NotBlank(message = "O nome é obrigatório.") String nome) {
    }

    @RestController
    static class ControllerDeTeste {

        @GetMapping("/teste/nao-encontrado")
        void naoEncontrado() {
            throw new RecursoNaoEncontradoException("empresa", 99);
        }

        @GetMapping("/teste/regra")
        void regra() {
            throw new RegraNegocioException("Empresa inativa não pode receber obrigações.");
        }

        @GetMapping("/teste/conflito")
        void conflito() {
            throw new ConflitoException("Já existe uma empresa com este CNPJ.");
        }

        @GetMapping("/teste/integridade")
        void integridade() {
            throw new DataIntegrityViolationException("Duplicate entry 'x' for key 'uk_empresa_cnpj'");
        }

        @GetMapping("/teste/concorrencia")
        void concorrencia() {
            throw new ObjectOptimisticLockingFailureException(Object.class, 1L);
        }

        @GetMapping("/teste/multipart")
        void multipartMalformado() {
            throw new MultipartException("Failed to parse multipart servlet request");
        }

        @GetMapping("/teste/bloqueio")
        void bloqueio() {
            throw new CannotAcquireLockException("Deadlock found when trying to get lock");
        }

        @PostMapping("/teste/validacao")
        void validacao(@Valid @RequestBody RequisicaoDeTeste requisicao) {
        }

        @GetMapping("/teste/parametro")
        void parametro(@RequestParam @Positive(message = "A página deve ser positiva.") int pagina) {
        }

        @GetMapping("/teste/id/{id}")
        void porId(@PathVariable Long id) {
        }

        @PostMapping("/teste/upload")
        void upload(@RequestPart("arquivo") MultipartFile arquivo) {
        }

        @GetMapping("/teste/upload-grande")
        void uploadGrande() {
            throw new MaxUploadSizeExceededException(-1);
        }

        @GetMapping("/teste/inesperado")
        void inesperado() {
            throw new IllegalStateException("detalhe interno que não deve vazar");
        }
    }
}
