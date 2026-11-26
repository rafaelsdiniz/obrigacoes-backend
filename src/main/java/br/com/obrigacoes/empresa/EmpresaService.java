package br.com.obrigacoes.empresa;

import br.com.obrigacoes.empresa.dto.EmpresaFiltro;
import br.com.obrigacoes.empresa.dto.EmpresaRequest;
import br.com.obrigacoes.shared.exception.ConflitoException;
import br.com.obrigacoes.shared.exception.RecursoNaoEncontradoException;
import br.com.obrigacoes.shared.exception.RegraNegocioException;
import br.com.obrigacoes.shared.validation.Cnpjs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmpresaService {

    private static final String RECURSO = "empresa";

    private final EmpresaRepository repository;

    public EmpresaService(EmpresaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Empresa criar(EmpresaRequest requisicao) {
        String cnpj = Cnpjs.normalizar(requisicao.cnpj());
        if (repository.existsByCnpj(cnpj)) {
            throw cnpjDuplicado();
        }
        Empresa empresa = new Empresa(requisicao.razaoSocial(), requisicao.nomeFantasia(), cnpj,
                requisicao.email(), requisicao.telefone());
        return repository.save(empresa);
    }

    @Transactional
    public Empresa atualizar(Long id, EmpresaRequest requisicao) {
        Empresa empresa = buscarPorId(id);
        empresa.verificarVersao(requisicao.versao());
        String cnpj = Cnpjs.normalizar(requisicao.cnpj());
        if (repository.existsByCnpjAndIdNot(cnpj, id)) {
            throw cnpjDuplicado();
        }
        empresa.atualizarDados(requisicao.razaoSocial(), requisicao.nomeFantasia(), cnpj,
                requisicao.email(), requisicao.telefone());
        return empresa;
    }

    public Empresa buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, id));
    }

    public Empresa buscarAtivaPorId(Long id) {
        Empresa empresa = buscarPorId(id);
        if (!empresa.isAtiva()) {
            throw new RegraNegocioException(
                    "A empresa '%s' está inativa e não pode receber novas atribuições.".formatted(empresa.getRazaoSocial()));
        }
        return empresa;
    }

    public Page<Empresa> listar(EmpresaFiltro filtro, Pageable paginacao) {
        return repository.findAll(EmpresaSpecifications.com(filtro), paginacao);
    }

    @Transactional
    public Empresa ativar(Long id) {
        Empresa empresa = buscarPorId(id);
        empresa.ativar();
        return empresa;
    }

    @Transactional
    public Empresa inativar(Long id) {
        Empresa empresa = buscarPorId(id);
        empresa.inativar();
        return empresa;
    }

    private static ConflitoException cnpjDuplicado() {
        return new ConflitoException("cnpj", "Já existe uma empresa cadastrada com este CNPJ.");
    }
}
