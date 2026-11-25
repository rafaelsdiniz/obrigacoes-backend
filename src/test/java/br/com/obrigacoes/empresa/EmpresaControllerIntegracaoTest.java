package br.com.obrigacoes.empresa;

import br.com.obrigacoes.IntegracaoTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmpresaControllerIntegracaoTest extends IntegracaoTest {

    private static final String URL = "/api/v1/empresas";

    @Test
    void deveCadastrarEmpresaAtivaComCnpjNormalizado() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("Contábil Alfa LTDA", "11.222.333/0001-81")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(URL + "/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cnpj").value("11222333000181"))
                .andExpect(jsonPath("$.situacao").value("ATIVO"))
                .andExpect(jsonPath("$.versao").value(0))
                .andExpect(jsonPath("$.criadoEm").isNotEmpty());
    }

    @Test
    void deveCadastrarEmpresaComCnpjAlfanumerico() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("Beta Serviços", "12.ABC.345/01DE-35")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cnpj").value("12ABC34501DE35"));
    }

    @Test
    void deveAceitarEmailComEspacosNasPontas() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"razaoSocial": "Alfa", "cnpj": "11222333000181", "email": "  Contato@Alfa.com.br  "}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("contato@alfa.com.br"));
    }

    @Test
    void deveRejeitarCadastroComCamposInvalidos() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"razaoSocial": "", "cnpj": "11.222.333/0001-82", "email": "invalido"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                .andExpect(jsonPath("$.erros[*].campo", containsInAnyOrder("razaoSocial", "cnpj", "email")));
    }

    @Test
    void deveRejeitarCnpjDuplicadoMesmoComMascaraDiferente() throws Exception {
        criar("Contábil Alfa LTDA", "11222333000181");

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("Outra", "11.222.333/0001-81")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Já existe uma empresa cadastrada com este CNPJ."));
    }

    @Test
    void deveAtualizarDadosEIncrementarVersao() throws Exception {
        long id = criar("Contábil Alfa LTDA", "11222333000181");

        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(jsonComVersao("Contábil Alfa S.A.", "11222333000181", 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razaoSocial").value("Contábil Alfa S.A."))
                .andExpect(jsonPath("$.versao").value(1));
    }

    @Test
    void naoDeveSobrescreverAlteracaoFeitaAPartirDeVersaoMaisNova() throws Exception {
        long id = criar("Contábil Alfa LTDA", "11222333000181");
        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(jsonComVersao("Edição da Ana", "11222333000181", 0)))
                .andExpect(status().isOk());

        // Outra pessoa ainda estava com a versão 0 aberta na tela
        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(jsonComVersao("Edição do Bruno", "11222333000181", 0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO"));

        mockMvc.perform(get(URL + "/" + id))
                .andExpect(jsonPath("$.razaoSocial").value("Edição da Ana"));
    }

    @Test
    void deveExigirVersaoNaAtualizacao() throws Exception {
        long id = criar("Contábil Alfa LTDA", "11222333000181");

        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(json("Alfa", "11222333000181")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("versao"));
    }

    @Test
    void naoDeveAtualizarParaCnpjDeOutraEmpresa() throws Exception {
        criar("Contábil Alfa LTDA", "11222333000181");
        long id = criar("Beta Serviços", "11444777000161");

        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(jsonComVersao("Beta Serviços", "11222333000181", 0)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar404ParaEmpresaInexistente() throws Exception {
        mockMvc.perform(get(URL + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
    }

    @Test
    void deveInativarEManterEmpresaDisponivelParaConsulta() throws Exception {
        long id = criar("Contábil Alfa LTDA", "11222333000181");

        mockMvc.perform(patch(URL + "/" + id + "/inativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("INATIVO"));

        mockMvc.perform(get(URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("INATIVO"));

        mockMvc.perform(patch(URL + "/" + id + "/ativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("ATIVO"));
    }

    @Test
    void deveListarComFiltrosEPaginacao() throws Exception {
        criar("Contábil Alfa LTDA", "11222333000181");
        criar("Beta Serviços", "11444777000161");
        inativar(criar("Gama Comércio", "12ABC34501DE35"));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tamanho").value(20))
                .andExpect(jsonPath("$.totalElementos").value(3));

        mockMvc.perform(get(URL).param("situacao", "ATIVO").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].razaoSocial").value("Beta Serviços"))
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(2));

        mockMvc.perform(get(URL).param("busca", "11.444.777"))
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].razaoSocial").value("Beta Serviços"));

        mockMvc.perform(get(URL).param("busca", "alfa"))
                .andExpect(jsonPath("$.conteudo[0].razaoSocial").value("Contábil Alfa LTDA"));
    }

    @Test
    void deveTratarCaracteresEspeciaisDaBuscaComoTextoLiteral() throws Exception {
        criar("Contábil Alfa LTDA", "11222333000181");
        criar("Beta 100% Digital", "11444777000161");

        mockMvc.perform(get(URL).param("busca", "%"))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].razaoSocial").value("Beta 100% Digital"));

        mockMvc.perform(get(URL).param("busca", "_"))
                .andExpect(jsonPath("$.totalElementos").value(0));

        mockMvc.perform(get(URL).param("busca", "/"))
                .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void deveRejeitarFiltroOrdenacaoEPaginaInvalidos() throws Exception {
        mockMvc.perform(get(URL).param("situacao", "SUSPENSO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("situacao"));

        mockMvc.perform(get(URL).param("sort", "versao"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.detail").value(startsWith("Não é possível ordenar pelo campo 'versao'.")));

        mockMvc.perform(get(URL).param("sort", "razaoSocial;DROP TABLE empresa"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get(URL).param("page", String.valueOf(Integer.MAX_VALUE)).param("size", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O número da página informado é muito alto."));
    }

    private long criar(String razaoSocial, String cnpj) throws Exception {
        String resposta = mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(razaoSocial, cnpj)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.<Number>read(resposta, "$.id").longValue();
    }

    private void inativar(long id) throws Exception {
        mockMvc.perform(patch(URL + "/" + id + "/inativar")).andExpect(status().isOk());
    }

    private static String json(String razaoSocial, String cnpj) {
        return """
                {"razaoSocial": "%s", "cnpj": "%s", "email": "contato@empresa.com.br"}
                """.formatted(razaoSocial, cnpj);
    }

    private static String jsonComVersao(String razaoSocial, String cnpj, long versao) {
        return """
                {"razaoSocial": "%s", "cnpj": "%s", "email": "contato@empresa.com.br", "versao": %d}
                """.formatted(razaoSocial, cnpj, versao);
    }
}
