package br.com.obrigacoes.usuario;

import br.com.obrigacoes.shared.exception.ConflitoException;
import br.com.obrigacoes.shared.exception.RegraNegocioException;
import br.com.obrigacoes.usuario.dto.UsuarioRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        when(repository.save(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

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
        verify(repository, never()).save(any());
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
}
