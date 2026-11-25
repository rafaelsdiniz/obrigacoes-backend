package br.com.obrigacoes.empresa;

import br.com.obrigacoes.empresa.dto.EmpresaFiltro;
import br.com.obrigacoes.empresa.dto.EmpresaRequest;
import br.com.obrigacoes.empresa.dto.EmpresaResponse;
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

import java.util.List;

@Tag(name = "Empresas", description = "Cadastro das empresas clientes do escritório")
@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresaController {

    private static final List<String> CAMPOS_ORDENACAO =
            List.of("razaoSocial", "nomeFantasia", "cnpj", "situacao", "criadoEm", "atualizadoEm");

    private final EmpresaService service;

    public EmpresaController(EmpresaService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastra uma empresa", description = "Toda empresa é criada com situação ATIVO.")
    @PostMapping
    public ResponseEntity<EmpresaResponse> criar(@Valid @RequestBody EmpresaRequest requisicao) {
        EmpresaResponse resposta = EmpresaResponse.de(service.criar(requisicao));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(location).body(resposta);
    }

    @Operation(summary = "Lista empresas com filtros e paginação")
    @GetMapping
    public PaginaResponse<EmpresaResponse> listar(
            @ParameterObject EmpresaFiltro filtro,
            @ParameterObject @PageableDefault(size = 20, sort = "razaoSocial", direction = Sort.Direction.ASC) Pageable paginacao) {
        return PaginaResponse.de(service.listar(filtro, Paginacao.validar(paginacao, CAMPOS_ORDENACAO)),
                EmpresaResponse::de);
    }

    @Operation(summary = "Consulta uma empresa pelo id")
    @GetMapping("/{id}")
    public EmpresaResponse buscar(@PathVariable Long id) {
        return EmpresaResponse.de(service.buscarPorId(id));
    }

    @Operation(summary = "Atualiza os dados cadastrais de uma empresa")
    @PutMapping("/{id}")
    public EmpresaResponse atualizar(@PathVariable Long id,
                                     @Validated(Atualizacao.class) @RequestBody EmpresaRequest requisicao) {
        return EmpresaResponse.de(service.atualizar(id, requisicao));
    }

    @Operation(summary = "Ativa uma empresa", description = "Operação idempotente.")
    @PatchMapping("/{id}/ativar")
    public EmpresaResponse ativar(@PathVariable Long id) {
        return EmpresaResponse.de(service.ativar(id));
    }

    @Operation(summary = "Inativa uma empresa",
            description = "A empresa continua disponível para consulta, mas não pode receber novas obrigações. Operação idempotente.")
    @PatchMapping("/{id}/inativar")
    public EmpresaResponse inativar(@PathVariable Long id) {
        return EmpresaResponse.de(service.inativar(id));
    }
}
