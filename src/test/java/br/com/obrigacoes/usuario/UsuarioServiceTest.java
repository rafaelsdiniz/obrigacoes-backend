package br.com.obrigacoes.usuario;

import br.com.obrigacoes.shared.exception.ConflitoException;
import br.com.obrigacoes.shared.exception.ErroCampo;
import br.com.obrigacoes.shared.exception.RegraNegocioException;
import br.com.obrigacoes.usuario.dto.UsuarioRequest;
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
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private UsuarioService service;

    @Test
    void deveCriarUsuarioComEmailEmMinusculas() {
        when(repository.existsByEmail("ana@escritorio.com.br")).thenReturn(false);
        when(repository.saveAndFlush(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Usuario usuario = service.criar(new UsuarioRequest("Ana Souza", " Ana@Escritorio.com.br ", "Analista Fiscal", null));

        assertThat(usuario.getEmail()).isEqualTo("ana@escritorio.com.br");
        assertThat(usuario.isAtivo()).isTrue();
    }

    @Test
    void naoDeveCriarUsuarioComEmailJaCadastradoIndependenteDaCaixa() {
        when(repository.existsByEmail("ana@escritorio.com.br")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new UsuarioRequest("Ana", "ANA@ESCRITORIO.COM.BR", null, null)))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe um usuário cadastrado com este e-mail.");
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void naoDeveAtualizarParaEmailDeOutroUsuario() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Usuario("Ana", "ana@escritorio.com.br", null)));
        when(repository.existsByEmailAndIdNot("bruno@escritorio.com.br", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, new UsuarioRequest("Ana", "bruno@escritorio.com.br", null, null)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void naoDeveDisponibilizarUsuarioInativoParaNovasAtribuicoes() {
        Usuario inativo = new Usuario("Ana Souza", "ana@escritorio.com.br", null);
        inativo.inativar();
        when(repository.findById(1L)).thenReturn(Optional.of(inativo));

        assertThatThrownBy(() -> service.buscarAtivoPorId(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("está inativo");
    }
    @Test
    void deveIndicarOCampoQuandoADuplicidadeSoEhDetectadaPeloBanco() {
        when(repository.saveAndFlush(any(Usuario.class))).thenThrow(new DataIntegrityViolationException("duplicado",
                new ConstraintViolationException("Duplicate entry", new SQLException(), "usuario.uk_usuario_email")));

        assertThatThrownBy(() -> service.criar(new UsuarioRequest("Ana", "ana@escritorio.com.br", null, null)))
                .isInstanceOfSatisfying(ConflitoException.class, erro ->
                        assertThat(erro.getErros()).extracting(ErroCampo::campo).containsExactly("email"));
    }

    @Test
    void deveRepassarViolacaoDeOutraRestricao() {
        DataIntegrityViolationException outra = new DataIntegrityViolationException("outra",
                new ConstraintViolationException("Check", new SQLException(), "ck_qualquer"));
        when(repository.saveAndFlush(any(Usuario.class))).thenThrow(outra);

        assertThatThrownBy(() -> service.criar(new UsuarioRequest("Ana", "ana@escritorio.com.br", null, null))).isSameAs(outra);
    }
}
