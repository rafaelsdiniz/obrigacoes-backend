package br.com.obrigacoes.usuario;

import br.com.obrigacoes.IntegracaoTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UsuarioControllerIntegracaoTest extends IntegracaoTest {

    private static final String URL = "/api/v1/usuarios";

    @Test
    void deveCadastrarUsuarioAtivoComEmailNormalizado() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("Ana Souza", "  Ana.Souza@Escritorio.com.br ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana.souza@escritorio.com.br"))
                .andExpect(jsonPath("$.situacao").value("ATIVO"))
                .andExpect(jsonPath("$.versao").value(0));
    }

    @Test
    void deveRejeitarCadastroComCamposInvalidos() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": " ", "email": "sem-arroba"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", containsInAnyOrder("nome", "email")));
    }

    @Test
    void deveRejeitarEmailDuplicadoIndependenteDaCaixa() throws Exception {
        criar("Ana Souza", "ana@escritorio.com.br");

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("Outra Ana", "ANA@ESCRITORIO.COM.BR")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO"))
                .andExpect(jsonPath("$.erros[0].campo").value("email"))
                .andExpect(jsonPath("$.erros[0].mensagem").value("Já existe um usuário cadastrado com este e-mail."));
    }

    @Test
    void deveDiferenciarEmailsQueSoMudamNoAcento() throws Exception {
        criar("João", "joão@escritorio.com.br");

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("Joao", "joao@escritorio.com.br")))
                .andExpect(status().isCreated());
    }

    @Test
    void deveAtualizarDadosDoUsuario() throws Exception {
        long id = criar("Ana Souza", "ana@escritorio.com.br");

        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "Ana Souza Lima", "email": "ana@escritorio.com.br", "cargo": "Coordenadora Fiscal", "versao": 0}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza Lima"))
                .andExpect(jsonPath("$.cargo").value("Coordenadora Fiscal"))
                .andExpect(jsonPath("$.versao").value(1));
    }

    @Test
    void naoDeveAtualizarAPartirDeVersaoDesatualizada() throws Exception {
        long id = criar("Ana Souza", "ana@escritorio.com.br");

        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "Ana", "email": "ana@escritorio.com.br", "versao": 7}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erros").doesNotExist());
    }

    @Test
    void deveInativarEManterUsuarioDisponivelParaConsulta() throws Exception {
        long id = criar("Ana Souza", "ana@escritorio.com.br");

        mockMvc.perform(patch(URL + "/" + id + "/inativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("INATIVO"));

        mockMvc.perform(get(URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("INATIVO"));
    }

    @Test
    void deveListarComFiltros() throws Exception {
        criar("Ana Souza", "ana@escritorio.com.br");
        long inativo = criar("Bruno Lima", "bruno@escritorio.com.br");
        mockMvc.perform(patch(URL + "/" + inativo + "/inativar")).andExpect(status().isOk());

        mockMvc.perform(get(URL).param("situacao", "ATIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].nome").value("Ana Souza"));

        mockMvc.perform(get(URL).param("busca", "BRUNO"))
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].situacao").value("INATIVO"));
    }

    private long criar(String nome, String email) throws Exception {
        String resposta = mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(nome, email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.<Number>read(resposta, "$.id").longValue();
    }

    private static String json(String nome, String email) {
        return """
                {"nome": "%s", "email": "%s", "cargo": "Analista Fiscal"}
                """.formatted(nome, email);
    }
}
