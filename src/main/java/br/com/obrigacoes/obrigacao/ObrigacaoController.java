package br.com.obrigacoes.obrigacao;

import br.com.obrigacoes.obrigacao.dto.AlteracaoStatusRequest;
import br.com.obrigacoes.obrigacao.dto.HistoricoStatusResponse;
import br.com.obrigacoes.obrigacao.dto.ObrigacaoFiltro;
import br.com.obrigacoes.obrigacao.dto.ObrigacaoRequest;
import br.com.obrigacoes.obrigacao.dto.ObrigacaoResponse;
import br.com.obrigacoes.obrigacao.dto.ResumoObrigacoesFiltro;
import br.com.obrigacoes.obrigacao.dto.ResumoObrigacoesResponse;
import br.com.obrigacoes.shared.validation.Atualizacao;
import br.com.obrigacoes.shared.web.PaginaResponse;
import br.com.obrigacoes.shared.web.Paginacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Obrigações", description = "Obrigações das empresas clientes e o andamento de cada uma")
@RestController
@RequestMapping("/api/v1/obrigacoes")
public class ObrigacaoController {

    private static final List<String> CAMPOS_ORDENACAO =
            List.of("titulo", "tipo", "competencia", "dataVencimento", "status", "criadoEm", "atualizadoEm");

    private final ObrigacaoService service;
    private final Clock clock;

    public ObrigacaoController(ObrigacaoService service, Clock clock) {
        this.service = service;
        this.clock = clock;
    }

    @Operation(summary = "Cadastra uma obrigação",
            description = "Toda obrigação inicia como PENDENTE. A empresa e o responsável precisam estar ativos.")
    @PostMapping
    public ResponseEntity<ObrigacaoResponse> criar(@Valid @RequestBody ObrigacaoRequest requisicao) {
        ObrigacaoResponse resposta = ObrigacaoResponse.de(service.criar(requisicao), hoje());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(location).body(resposta);
    }

    @Operation(summary = "Lista obrigações com filtros e paginação")
    @GetMapping
    public PaginaResponse<ObrigacaoResponse> listar(
            @ParameterObject ObrigacaoFiltro filtro,
            @ParameterObject @PageableDefault(size = 20, sort = "dataVencimento", direction = Sort.Direction.ASC) Pageable paginacao) {
        LocalDate hoje = hoje();
        return PaginaResponse.de(service.listar(filtro, hoje, Paginacao.validar(paginacao, CAMPOS_ORDENACAO)),
                obrigacao -> ObrigacaoResponse.de(obrigacao, hoje));
    }

    @Operation(summary = "Resume as obrigações por status e as atrasadas",
            description = "Pode ser restrito a uma empresa e/ou a um responsável.")
    @GetMapping("/resumo")
    public ResumoObrigacoesResponse resumir(@ParameterObject ResumoObrigacoesFiltro filtro) {
        return ResumoObrigacoesResponse.de(service.resumir(filtro, hoje()));
    }

    @Operation(summary = "Consulta uma obrigação pelo id")
    @GetMapping("/{id}")
    public ObrigacaoResponse buscar(@PathVariable Long id) {
        return ObrigacaoResponse.de(service.buscarPorId(id), hoje());
    }

    @Operation(summary = "Atualiza os dados de uma obrigação",
            description = "Não altera o status. Obrigações concluídas não podem ser editadas; uma nova empresa ou responsável precisa estar ativo.")
    @PutMapping("/{id}")
    public ObrigacaoResponse atualizar(@PathVariable Long id,
                                       @Validated(Atualizacao.class) @RequestBody ObrigacaoRequest requisicao) {
        return ObrigacaoResponse.de(service.atualizar(id, requisicao), hoje());
    }

    @Operation(summary = "Avança o status de uma obrigação",
            description = "Fluxo permitido: PENDENTE → EM_ANDAMENTO → CONCLUIDA. Não é possível pular etapas, retroceder ou alterar uma obrigação concluída.")
    @PatchMapping("/{id}/status")
    public ObrigacaoResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody AlteracaoStatusRequest requisicao) {
        return ObrigacaoResponse.de(service.alterarStatus(id, requisicao.status()), hoje());
    }

    @Operation(summary = "Lista o histórico de status de uma obrigação")
    @GetMapping("/{id}/historico")
    public List<HistoricoStatusResponse> historico(@PathVariable Long id) {
        return service.listarHistorico(id).stream().map(HistoricoStatusResponse::de).toList();
    }

    private LocalDate hoje() {
        return LocalDate.now(clock);
    }
}
