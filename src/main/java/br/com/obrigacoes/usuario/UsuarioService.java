package br.com.obrigacoes.usuario;

import br.com.obrigacoes.shared.domain.Textos;
import br.com.obrigacoes.shared.exception.ConflitoException;
import br.com.obrigacoes.shared.exception.RecursoNaoEncontradoException;
import br.com.obrigacoes.shared.exception.RegraNegocioException;
import br.com.obrigacoes.usuario.dto.UsuarioFiltro;
import br.com.obrigacoes.usuario.dto.UsuarioRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private static final String RECURSO = "Usuário";

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Usuario criar(UsuarioRequest requisicao) {
        String email = Textos.normalizarEmail(requisicao.email());
        if (repository.existsByEmail(email)) {
            throw emailDuplicado();
        }
        return repository.save(new Usuario(requisicao.nome(), email, requisicao.cargo()));
    }

    @Transactional
    public Usuario atualizar(Long id, UsuarioRequest requisicao) {
        Usuario usuario = buscarPorId(id);
        usuario.verificarVersao(requisicao.versao());
        String email = Textos.normalizarEmail(requisicao.email());
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw emailDuplicado();
        }
        usuario.atualizarDados(requisicao.nome(), email, requisicao.cargo());
        return usuario;
    }

    public Usuario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, id));
    }

    public Usuario buscarAtivoPorId(Long id) {
        Usuario usuario = buscarPorId(id);
        if (!usuario.isAtivo()) {
            throw new RegraNegocioException(
                    "O usuário '%s' está inativo e não pode receber novas atribuições.".formatted(usuario.getNome()));
        }
        return usuario;
    }

    public Page<Usuario> listar(UsuarioFiltro filtro, Pageable paginacao) {
        return repository.findAll(UsuarioSpecifications.com(filtro), paginacao);
    }

    @Transactional
    public Usuario ativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.ativar();
        return usuario;
    }

    @Transactional
    public Usuario inativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.inativar();
        return usuario;
    }

    private static ConflitoException emailDuplicado() {
        return new ConflitoException("Já existe um usuário cadastrado com este e-mail.");
    }
}
