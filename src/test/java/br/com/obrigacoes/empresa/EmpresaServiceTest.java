package br.com.obrigacoes.empresa;

import br.com.obrigacoes.empresa.dto.EmpresaRequest;
import br.com.obrigacoes.shared.exception.ConflitoException;
import br.com.obrigacoes.shared.exception.ErroCampo;
import br.com.obrigacoes.shared.exception.RecursoNaoEncontradoException;
import br.com.obrigacoes.shared.exception.RegraNegocioException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    private static final String CNPJ = "11222333000181";

    @Mock
    private EmpresaRepository repository;

    @InjectMocks
    private EmpresaService service;

    @Test
    void deveCriarEmpresaComCnpjNormalizado() {
        when(repository.existsByCnpj(CNPJ)).thenReturn(false);
        when(repository.saveAndFlush(any(Empresa.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Empresa empresa = service.criar(requisicao("11.222.333/0001-81"));

        assertThat(empresa.getCnpj()).isEqualTo(CNPJ);
        assertThat(empresa.isAtiva()).isTrue();
    }

    @Test
    void naoDeveCriarEmpresaComCnpjJaCadastrado() {
        when(repository.existsByCnpj(CNPJ)).thenReturn(true);

        assertThatThrownBy(() -> service.criar(requisicao("11.222.333/0001-81")))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe uma empresa cadastrada com este CNPJ.");
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void devePermitirAtualizarMantendoOProprioCnpj() {
        Empresa existente = new Empresa("Antiga", null, CNPJ, null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByCnpjAndIdNot(CNPJ, 1L)).thenReturn(false);

        Empresa atualizada = service.atualizar(1L, requisicao(CNPJ));

        assertThat(atualizada.getRazaoSocial()).isEqualTo("Contábil Alfa LTDA");
    }

    @Test
    void naoDeveAtualizarParaCnpjDeOutraEmpresa() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Empresa("Antiga", null, "11444777000161", null, null)));
        when(repository.existsByCnpjAndIdNot(CNPJ, 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, requisicao(CNPJ)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void naoDeveAtualizarAPartirDeUmaVersaoDesatualizada() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Empresa("Antiga", null, CNPJ, null, null)));
        EmpresaRequest versaoAntiga = new EmpresaRequest("Nova", null, CNPJ, null, null, 3L);

        assertThatThrownBy(() -> service.atualizar(1L, versaoAntiga))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("alterado por outra operação");
    }

    @Test
    void deveLancarNaoEncontradoParaIdInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Não existe empresa com o id 99.");
    }

    @Test
    void naoDeveDisponibilizarEmpresaInativaParaNovasAtribuicoes() {
        Empresa inativa = new Empresa("Contábil Alfa LTDA", null, CNPJ, null, null);
        inativa.inativar();
        when(repository.findById(1L)).thenReturn(Optional.of(inativa));

        assertThatThrownBy(() -> service.buscarAtivaPorId(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("está inativa");
    }

    @Test
    void deveRetornarEmpresaAtivaParaNovasAtribuicoes() {
        Empresa ativa = new Empresa("Contábil Alfa LTDA", null, CNPJ, null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(ativa));

        assertThat(service.buscarAtivaPorId(1L)).isSameAs(ativa);
    }

    private static EmpresaRequest requisicao(String cnpj) {
        return new EmpresaRequest("Contábil Alfa LTDA", "Alfa", cnpj, "contato@alfa.com.br", "6332140000", null);
    }
    @Test
    void deveIndicarOCampoQuandoADuplicidadeSoEhDetectadaPeloBanco() {
        when(repository.saveAndFlush(any(Empresa.class))).thenThrow(new DataIntegrityViolationException("duplicado",
                new ConstraintViolationException("Duplicate entry", new SQLException(), "empresa.uk_empresa_cnpj")));

        assertThatThrownBy(() -> service.criar(requisicao("11.222.333/0001-81")))
                .isInstanceOfSatisfying(ConflitoException.class, erro ->
                        assertThat(erro.getErros()).extracting(ErroCampo::campo).containsExactly("cnpj"));
    }

    @Test
    void deveRepassarViolacaoDeOutraRestricao() {
        DataIntegrityViolationException outra = new DataIntegrityViolationException("outra",
                new ConstraintViolationException("Check", new SQLException(), "ck_qualquer"));
        when(repository.saveAndFlush(any(Empresa.class))).thenThrow(outra);

        assertThatThrownBy(() -> service.criar(requisicao("11.222.333/0001-81"))).isSameAs(outra);
    }
}
