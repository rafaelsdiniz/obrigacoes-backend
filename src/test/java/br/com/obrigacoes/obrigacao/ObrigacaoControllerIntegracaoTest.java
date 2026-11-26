package br.com.obrigacoes.obrigacao;

import br.com.obrigacoes.IntegracaoTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ObrigacaoControllerIntegracaoTest extends IntegracaoTest {

    private static final String URL = "/api/v1/obrigacoes";
    private static final LocalDate HOJE = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

    private long empresaId;
    private long responsavelId;

    @BeforeEach
    void cadastrarEmpresaEResponsavel() throws Exception {
        empresaId = criarEmpresa("Contábil Alfa LTDA", "11222333000181");
        responsavelId = criarUsuario("Ana Souza", "ana@escritorio.com.br");
    }

    @Test
    void deveCadastrarObrigacaoPendenteComEmpresaEResponsavel() throws Exception {
        criar("Entrega da DCTFWeb", HOJE.plusDays(10), empresaId, responsavelId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.proximoStatus").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.atrasada").value(false))
                .andExpect(jsonPath("$.tipo").value("PREVIDENCIARIA"))
                .andExpect(jsonPath("$.competencia").value("2026-10"))
                .andExpect(jsonPath("$.dataVencimento").value(HOJE.plusDays(10).toString()))
                .andExpect(jsonPath("$.empresa.id").value(empresaId))
                .andExpect(jsonPath("$.empresa.razaoSocial").value("Contábil Alfa LTDA"))
                .andExpect(jsonPath("$.responsavel.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.versao").value(0));
    }

    @Test
    void deveIgnorarStatusEnviadoNoCadastro() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"titulo": "DCTFWeb", "tipo": "FISCAL", "dataVencimento": "%s",
                         "empresaId": %d, "responsavelId": %d, "status": "CONCLUIDA"}
                        """.formatted(HOJE, empresaId, responsavelId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void naoDeveAtribuirObrigacaoAEmpresaOuResponsavelInativo() throws Exception {
        mockMvc.perform(patch("/api/v1/empresas/" + empresaId + "/inativar")).andExpect(status().isOk());
        criar("DCTFWeb", HOJE, empresaId, responsavelId)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("REGRA_NEGOCIO"))
                .andExpect(jsonPath("$.detail")
                        .value("A empresa 'Contábil Alfa LTDA' está inativa e não pode receber novas atribuições."));

        long empresaAtiva = criarEmpresa("Beta Serviços", "11444777000161");
        mockMvc.perform(patch("/api/v1/usuarios/" + responsavelId + "/inativar")).andExpect(status().isOk());
        criar("DCTFWeb", HOJE, empresaAtiva, responsavelId)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("O usuário 'Ana Souza' está inativo e não pode receber novas atribuições."));
    }

    @Test
    void deveRetornar404ParaEmpresaOuResponsavelInexistente() throws Exception {
        criar("DCTFWeb", HOJE, 999, responsavelId).andExpect(status().isNotFound());
        criar("DCTFWeb", HOJE, empresaId, 999).andExpect(status().isNotFound());
    }

    @Test
    void deveValidarCamposObrigatoriosEFormatos() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo",
                        containsInAnyOrder("titulo", "tipo", "dataVencimento", "empresaId", "responsavelId")));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"titulo": "DCTFWeb", "tipo": "IMPOSTO", "dataVencimento": "%s", "empresaId": 1, "responsavelId": 1}
                        """.formatted(HOJE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("tipo"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"titulo": "DCTFWeb", "tipo": "FISCAL", "dataVencimento": "31/12/2026", "empresaId": 1, "responsavelId": 1}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("dataVencimento"));
    }

    @Test
    void deveRejeitarDatasForaDoIntervaloAceitoEIdsDecimais() throws Exception {
        criar("DCTFWeb", LocalDate.of(1999, 12, 31), empresaId, responsavelId)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("dataVencimento"))
                .andExpect(jsonPath("$.erros[0].mensagem").value("A data de vencimento deve estar entre os anos 2000 e 2100."));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"titulo": "DCTFWeb", "tipo": "FISCAL", "dataVencimento": "0000-01-01", "empresaId": %d, "responsavelId": %d}
                        """.formatted(empresaId, responsavelId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"titulo": "DCTFWeb", "tipo": "FISCAL", "competencia": "2101-01", "dataVencimento": "%s",
                         "empresaId": %d, "responsavelId": %d}
                        """.formatted(HOJE, empresaId, responsavelId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("competencia"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"titulo": "DCTFWeb", "tipo": "FISCAL", "dataVencimento": "%s", "empresaId": %d.9, "responsavelId": %d}
                        """.formatted(HOJE, empresaId, responsavelId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("empresaId"));
    }

    @Test
    void deveSeguirOFluxoDeStatusERegistrarOHistorico() throws Exception {
        long id = idDe(criar("DCTFWeb", HOJE.plusDays(5), empresaId, responsavelId));

        alterarStatus(id, "EM_ANDAMENTO")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.proximoStatus").value("CONCLUIDA"));
        alterarStatus(id, "CONCLUIDA")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.proximoStatus").value(nullValue()));

        mockMvc.perform(get(URL + "/" + id + "/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].statusAnterior").value(nullValue()))
                .andExpect(jsonPath("$[0].statusNovo").value("PENDENTE"))
                .andExpect(jsonPath("$[1].statusNovo").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$[2].statusAnterior").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$[2].statusNovo").value("CONCLUIDA"));
    }

    @Test
    void naoDevePularRetrocederNemAlterarObrigacaoConcluida() throws Exception {
        long id = idDe(criar("DCTFWeb", HOJE, empresaId, responsavelId));

        alterarStatus(id, "CONCLUIDA")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("REGRA_NEGOCIO"));

        alterarStatus(id, "EM_ANDAMENTO").andExpect(status().isOk());
        alterarStatus(id, "PENDENTE").andExpect(status().isUnprocessableContent());

        alterarStatus(id, "CONCLUIDA").andExpect(status().isOk());
        alterarStatus(id, "EM_ANDAMENTO")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("A obrigação já foi concluída e não pode mais ser alterada."));

        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("Novo título", HOJE, empresaId, responsavelId, 2)))
                .andExpect(status().isUnprocessableContent());

        mockMvc.perform(get(URL + "/" + id + "/historico")).andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void deveRejeitarStatusAusenteOuDesconhecido() throws Exception {
        long id = idDe(criar("DCTFWeb", HOJE, empresaId, responsavelId));

        mockMvc.perform(patch(URL + "/" + id + "/status").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("status"));

        alterarStatus(id, "CANCELADA")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("status"));
    }

    @Test
    void naoDeveAceitarStatusInformadoPelaPosicaoNumerica() throws Exception {
        long id = idDe(criar("DCTFWeb", HOJE, empresaId, responsavelId));

        mockMvc.perform(patch(URL + "/" + id + "/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\": 1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("status"));
        mockMvc.perform(get(URL + "/" + id)).andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void deveSerializarAlteracoesDeStatusConcorrentes() throws Exception {
        long id = idDe(criar("DCTFWeb", HOJE, empresaId, responsavelId));
        int requisicoes = 8;
        ExecutorService executor = Executors.newFixedThreadPool(requisicoes);
        CountDownLatch largada = new CountDownLatch(1);
        try {
            List<Future<Integer>> respostas = new ArrayList<>();
            for (int i = 0; i < requisicoes; i++) {
                respostas.add(executor.submit(() -> {
                    largada.await();
                    return alterarStatus(id, "EM_ANDAMENTO").andReturn().getResponse().getStatus();
                }));
            }
            largada.countDown();
            List<Integer> codigos = new ArrayList<>();
            for (Future<Integer> resposta : respostas) {
                codigos.add(resposta.get(30, TimeUnit.SECONDS));
            }

            assertThat(codigos).containsOnly(200, 422);
            assertThat(codigos).filteredOn(codigo -> codigo == 200).hasSize(1);
        } finally {
            executor.shutdownNow();
        }

        mockMvc.perform(get(URL + "/" + id + "/historico")).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void deveAtualizarDadosSemAlterarOStatus() throws Exception {
        long id = idDe(criar("DCTFWeb", HOJE, empresaId, responsavelId));
        alterarStatus(id, "EM_ANDAMENTO").andExpect(status().isOk());

        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("DCTFWeb - retificadora", HOJE.plusDays(3), empresaId, responsavelId, 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("DCTFWeb - retificadora"))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.versao").value(2));
    }

    @Test
    void deveManterResponsavelInativoMasNaoPermitirTrocarParaUmInativo() throws Exception {
        long id = idDe(criar("DCTFWeb", HOJE, empresaId, responsavelId));
        mockMvc.perform(patch("/api/v1/usuarios/" + responsavelId + "/inativar")).andExpect(status().isOk());

        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("DCTFWeb ajustada", HOJE, empresaId, responsavelId, 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsavel.situacao").value("INATIVO"));

        long outroInativo = criarUsuario("Bruno Lima", "bruno@escritorio.com.br");
        mockMvc.perform(patch("/api/v1/usuarios/" + outroInativo + "/inativar")).andExpect(status().isOk());
        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("DCTFWeb ajustada", HOJE, empresaId, outroInativo, 1)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void naoDeveSobrescreverEdicaoFeitaAPartirDeVersaoMaisNova() throws Exception {
        long id = idDe(criar("DCTFWeb", HOJE, empresaId, responsavelId));
        alterarStatus(id, "EM_ANDAMENTO").andExpect(status().isOk());

        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("Edição com versão antiga", HOJE, empresaId, responsavelId, 0)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveListarComFiltros() throws Exception {
        long outraEmpresa = criarEmpresa("Beta Serviços", "11444777000161");
        long atrasada = idDe(criar("FGTS Digital", HOJE.minusDays(2), empresaId, responsavelId));
        long emAndamento = idDe(criar("DCTFWeb", HOJE.plusDays(5), empresaId, responsavelId));
        alterarStatus(emAndamento, "EM_ANDAMENTO").andExpect(status().isOk());
        long vencidaConcluida = idDe(criar("EFD-Reinf", HOJE.minusDays(10), outraEmpresa, responsavelId));
        alterarStatus(vencidaConcluida, "EM_ANDAMENTO").andExpect(status().isOk());
        alterarStatus(vencidaConcluida, "CONCLUIDA").andExpect(status().isOk());

        mockMvc.perform(get(URL))
                .andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.conteudo[0].titulo").value("EFD-Reinf"));

        mockMvc.perform(get(URL).param("atrasada", "true"))
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].id").value(atrasada))
                .andExpect(jsonPath("$.conteudo[0].atrasada").value(true));

        mockMvc.perform(get(URL).param("atrasada", "false"))
                .andExpect(jsonPath("$.totalElementos").value(2));

        mockMvc.perform(get(URL).param("status", "EM_ANDAMENTO"))
                .andExpect(jsonPath("$.conteudo[*].id", containsInAnyOrder((int) emAndamento)));

        mockMvc.perform(get(URL).param("status", "PENDENTE", "EM_ANDAMENTO"))
                .andExpect(jsonPath("$.conteudo[*].id", containsInAnyOrder((int) atrasada, (int) emAndamento)));

        mockMvc.perform(get(URL).param("empresaId", String.valueOf(outraEmpresa)))
                .andExpect(jsonPath("$.conteudo[*].titulo", containsInAnyOrder("EFD-Reinf")));

        mockMvc.perform(get(URL).param("vencimentoDe", HOJE.minusDays(3).toString())
                        .param("vencimentoAte", HOJE.plusDays(1).toString()))
                .andExpect(jsonPath("$.conteudo[*].titulo", containsInAnyOrder("FGTS Digital")));

        mockMvc.perform(get(URL).param("busca", "dctf"))
                .andExpect(jsonPath("$.conteudo[*].titulo", containsInAnyOrder("DCTFWeb")));

        mockMvc.perform(get(URL).param("sort", "titulo,desc"))
                .andExpect(jsonPath("$.conteudo[0].titulo").value("FGTS Digital"));
    }

    @Test
    void deveResumirObrigacoesPorStatusEAtrasadas() throws Exception {
        long outraEmpresa = criarEmpresa("Beta Serviços", "11444777000161");
        long outroResponsavel = criarUsuario("Bruno Lima", "bruno@escritorio.com.br");
        criar("FGTS Digital", HOJE.minusDays(2), empresaId, responsavelId).andExpect(status().isCreated());
        criar("DCTFWeb", HOJE.plusDays(5), empresaId, responsavelId).andExpect(status().isCreated());
        long emAndamentoVencida = idDe(criar("EFD-Reinf", HOJE.minusDays(1), empresaId, outroResponsavel));
        alterarStatus(emAndamentoVencida, "EM_ANDAMENTO").andExpect(status().isOk());
        long concluidaVencida = idDe(criar("DEFIS", HOJE.minusDays(10), outraEmpresa, responsavelId));
        alterarStatus(concluidaVencida, "EM_ANDAMENTO").andExpect(status().isOk());
        alterarStatus(concluidaVencida, "CONCLUIDA").andExpect(status().isOk());

        resumir(null, null).andExpect(resumoIgual(2, 1, 1, 2));
        resumir(empresaId, null).andExpect(resumoIgual(2, 1, 0, 2));
        resumir(null, responsavelId).andExpect(resumoIgual(2, 0, 1, 1));
        resumir(empresaId, responsavelId).andExpect(resumoIgual(2, 0, 0, 1));
        resumir(outraEmpresa, outroResponsavel).andExpect(resumoIgual(0, 0, 0, 0));
    }

    @Test
    void deveRetornar404ParaObrigacaoInexistente() throws Exception {
        mockMvc.perform(get(URL + "/999")).andExpect(status().isNotFound());
        mockMvc.perform(get(URL + "/999/historico")).andExpect(status().isNotFound());
        alterarStatus(999, "EM_ANDAMENTO").andExpect(status().isNotFound());
    }

    private ResultActions criar(String titulo, LocalDate vencimento, long empresa, long responsavel) throws Exception {
        return mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(json(titulo, vencimento, empresa, responsavel, null)));
    }

    private ResultActions alterarStatus(long id, String novoStatus) throws Exception {
        return mockMvc.perform(patch(URL + "/" + id + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"%s\"}".formatted(novoStatus)));
    }

    private ResultActions resumir(Long empresa, Long responsavel) throws Exception {
        var requisicao = get(URL + "/resumo");
        if (empresa != null) {
            requisicao.param("empresaId", empresa.toString());
        }
        if (responsavel != null) {
            requisicao.param("responsavelId", responsavel.toString());
        }
        return mockMvc.perform(requisicao).andExpect(status().isOk());
    }

    private static ResultMatcher resumoIgual(long pendentes, long emAndamento, long concluidas, long atrasadas) {
        return resultado -> {
            jsonPath("$.pendentes").value(pendentes).match(resultado);
            jsonPath("$.emAndamento").value(emAndamento).match(resultado);
            jsonPath("$.concluidas").value(concluidas).match(resultado);
            jsonPath("$.atrasadas").value(atrasadas).match(resultado);
        };
    }

    private long criarEmpresa(String razaoSocial, String cnpj) throws Exception {
        return idDe(mockMvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"razaoSocial\": \"%s\", \"cnpj\": \"%s\"}".formatted(razaoSocial, cnpj))));
    }

    private long criarUsuario(String nome, String email) throws Exception {
        return idDe(mockMvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"%s\", \"email\": \"%s\"}".formatted(nome, email))));
    }

    private static long idDe(ResultActions resultado) throws Exception {
        String corpo = resultado.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.<Number>read(corpo, "$.id").longValue();
    }

    private static String json(String titulo, LocalDate vencimento, long empresa, long responsavel, Integer versao) {
        return """
                {"titulo": "%s", "descricao": "Envio mensal", "tipo": "PREVIDENCIARIA", "competencia": "2026-10",
                 "dataVencimento": "%s", "empresaId": %d, "responsavelId": %d, "versao": %s}
                """.formatted(titulo, vencimento, empresa, responsavel, versao);
    }
}
