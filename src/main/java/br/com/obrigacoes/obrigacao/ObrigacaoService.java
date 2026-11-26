package br.com.obrigacoes.obrigacao;

import br.com.obrigacoes.empresa.Empresa;
import br.com.obrigacoes.empresa.EmpresaService;
import br.com.obrigacoes.obrigacao.dto.ObrigacaoFiltro;
import br.com.obrigacoes.obrigacao.dto.ObrigacaoRequest;
import br.com.obrigacoes.obrigacao.dto.ResumoObrigacoesFiltro;
import br.com.obrigacoes.shared.exception.RecursoNaoEncontradoException;
import br.com.obrigacoes.usuario.Usuario;
import br.com.obrigacoes.usuario.UsuarioService;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class ObrigacaoService {

    private static final String RECURSO = "obrigação";

    private final ObrigacaoRepository repository;
    private final HistoricoStatusObrigacaoRepository historicoRepository;
    private final EmpresaService empresaService;
    private final UsuarioService usuarioService;
    private final Clock clock;

    public ObrigacaoService(ObrigacaoRepository repository,
                            HistoricoStatusObrigacaoRepository historicoRepository,
                            EmpresaService empresaService,
                            UsuarioService usuarioService,
                            Clock clock) {
        this.repository = repository;
        this.historicoRepository = historicoRepository;
        this.empresaService = empresaService;
        this.usuarioService = usuarioService;
        this.clock = clock;
    }

    @Transactional
    public Obrigacao criar(ObrigacaoRequest requisicao) {
        Empresa empresa = empresaService.buscarAtivaPorId(requisicao.empresaId());
        Usuario responsavel = usuarioService.buscarAtivoPorId(requisicao.responsavelId());
        Obrigacao obrigacao = new Obrigacao(requisicao.titulo(), requisicao.descricao(), requisicao.tipo(),
                requisicao.competencia(), requisicao.dataVencimento(), empresa, responsavel, Instant.now(clock));
        return repository.save(obrigacao);
    }

    @Transactional
    public Obrigacao atualizar(Long id, ObrigacaoRequest requisicao) {
        Obrigacao obrigacao = buscarPorId(id);
        obrigacao.verificarVersao(requisicao.versao());
        Empresa empresa = Objects.equals(obrigacao.getEmpresa().getId(), requisicao.empresaId())
                ? obrigacao.getEmpresa()
                : empresaService.buscarAtivaPorId(requisicao.empresaId());
        Usuario responsavel = Objects.equals(obrigacao.getResponsavel().getId(), requisicao.responsavelId())
                ? obrigacao.getResponsavel()
                : usuarioService.buscarAtivoPorId(requisicao.responsavelId());
        obrigacao.atualizarDados(requisicao.titulo(), requisicao.descricao(), requisicao.tipo(),
                requisicao.competencia(), requisicao.dataVencimento(), empresa, responsavel);
        return obrigacao;
    }

    @Transactional
    public Obrigacao alterarStatus(Long id, StatusObrigacao novoStatus) {
        Obrigacao obrigacao = buscarComBloqueio(id);
        obrigacao.alterarStatus(novoStatus, Instant.now(clock));
        Hibernate.initialize(obrigacao.getEmpresa());
        Hibernate.initialize(obrigacao.getResponsavel());
        return obrigacao;
    }

    public Obrigacao buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, id));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Obrigacao buscarComBloqueio(Long id) {
        return repository.buscarComBloqueio(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, id));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Obrigacao referenciaPara(Long id) {
        return repository.getReferenceById(id);
    }

    public void verificarExistencia(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException(RECURSO, id);
        }
    }

    public List<HistoricoStatusObrigacao> listarHistorico(Long id) {
        verificarExistencia(id);
        return historicoRepository.findByObrigacaoIdOrderByAlteradoEmAscIdAsc(id);
    }

    public Page<Obrigacao> listar(ObrigacaoFiltro filtro, LocalDate hoje, Pageable paginacao) {
        return repository.findAll(ObrigacaoSpecifications.com(filtro, hoje), paginacao);
    }

    public ResumoObrigacoes resumir(ResumoObrigacoesFiltro filtro, LocalDate hoje) {
        Specification<Obrigacao> escopo = ObrigacaoSpecifications.doEscopo(filtro.empresaId(), filtro.responsavelId());
        return new ResumoObrigacoes(
                repository.count(escopo.and(ObrigacaoSpecifications.comStatus(StatusObrigacao.PENDENTE))),
                repository.count(escopo.and(ObrigacaoSpecifications.comStatus(StatusObrigacao.EM_ANDAMENTO))),
                repository.count(escopo.and(ObrigacaoSpecifications.comStatus(StatusObrigacao.CONCLUIDA))),
                repository.count(escopo.and(ObrigacaoSpecifications.atrasadas(hoje)))
        );
    }
}
