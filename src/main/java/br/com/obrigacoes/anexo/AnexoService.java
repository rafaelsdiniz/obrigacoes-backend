package br.com.obrigacoes.anexo;

import br.com.obrigacoes.anexo.LimpezaDeArquivos.AnexoExcluido;
import br.com.obrigacoes.anexo.ValidadorArquivo.ArquivoValidado;
import br.com.obrigacoes.obrigacao.Obrigacao;
import br.com.obrigacoes.obrigacao.ObrigacaoService;
import br.com.obrigacoes.shared.exception.RecursoNaoEncontradoException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

@Service
@Transactional(readOnly = true)
class AnexoService {

    private static final String RECURSO = "anexo";

    private final AnexoRepository repository;
    private final ObrigacaoService obrigacaoService;
    private final ValidadorArquivo validador;
    private final ArmazenamentoArquivos armazenamento;
    private final ApplicationEventPublisher eventos;
    private final TransactionOperations transacao;

    AnexoService(AnexoRepository repository,
                 ObrigacaoService obrigacaoService,
                 ValidadorArquivo validador,
                 ArmazenamentoArquivos armazenamento,
                 ApplicationEventPublisher eventos,
                 TransactionOperations transacao) {
        this.repository = repository;
        this.obrigacaoService = obrigacaoService;
        this.validador = validador;
        this.armazenamento = armazenamento;
        this.eventos = eventos;
        this.transacao = transacao;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Anexo anexar(Long obrigacaoId, MultipartFile arquivo) {
        obrigacaoService.verificarExistencia(obrigacaoId);
        ArquivoValidado validado = validador.validar(arquivo);
        ArquivoArmazenado armazenado = armazenar(obrigacaoId, arquivo);
        try {
            return transacao.execute(status -> repository.save(new Anexo(
                    obrigacaoService.referenciaPara(obrigacaoId), validado.nome(), validado.tipo(), armazenado)));
        } catch (RuntimeException falhaAoRegistrar) {
            descartar(armazenado, falhaAoRegistrar);
            throw falhaAoRegistrar;
        }
    }

    public List<Anexo> listar(Long obrigacaoId) {
        obrigacaoService.verificarExistencia(obrigacaoId);
        return repository.findByObrigacaoIdOrderByCriadoEmDescIdDesc(obrigacaoId);
    }

    public ConteudoAnexo baixar(Long obrigacaoId, Long anexoId) {
        obrigacaoService.verificarExistencia(obrigacaoId);
        Anexo anexo = buscar(obrigacaoId, anexoId);
        return new ConteudoAnexo(anexo, armazenamento.abrir(anexo.getChaveArmazenamento()));
    }

    @Transactional
    public void excluir(Long obrigacaoId, Long anexoId) {
        Obrigacao obrigacao = obrigacaoService.buscarComBloqueio(obrigacaoId);
        Anexo anexo = buscar(obrigacaoId, anexoId);
        obrigacao.garantirQuePermiteExcluirAnexos();
        repository.delete(anexo);
        eventos.publishEvent(new AnexoExcluido(anexo.getChaveArmazenamento()));
    }

    private Anexo buscar(Long obrigacaoId, Long anexoId) {
        return repository.findByIdAndObrigacaoId(anexoId, obrigacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, anexoId));
    }

    private ArquivoArmazenado armazenar(Long obrigacaoId, MultipartFile arquivo) {
        try (InputStream conteudo = arquivo.getInputStream()) {
            return armazenamento.salvar(obrigacaoId.toString(), conteudo);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo enviado", e);
        }
    }

    private void descartar(ArquivoArmazenado armazenado, RuntimeException causa) {
        try {
            armazenamento.remover(armazenado.chave());
        } catch (RuntimeException falhaNaRemocao) {
            causa.addSuppressed(falhaNaRemocao);
        }
    }

    record ConteudoAnexo(Anexo anexo, Resource conteudo) {
    }
}
