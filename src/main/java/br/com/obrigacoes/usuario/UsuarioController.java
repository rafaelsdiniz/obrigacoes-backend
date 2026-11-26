package br.com.obrigacoes.usuario;

import br.com.obrigacoes.shared.validation.Atualizacao;
import br.com.obrigacoes.shared.web.PaginaResponse;
import br.com.obrigacoes.shared.web.Paginacao;
import br.com.obrigacoes.usuario.dto.UsuarioFiltro;
import br.com.obrigacoes.usuario.dto.UsuarioRequest;
import br.com.obrigacoes.usuario.dto.UsuarioResponse;
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

@Tag(name = "Usuários", description = "Cadastro dos colaboradores responsáveis pelas obrigações")
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private static final List<String> CAMPOS_ORDENACAO =
            List.of("nome", "email", "cargo", "situacao", "criadoEm", "atualizadoEm");

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastra um usuário", description = "Todo usuário é criado com situação ATIVO.")
    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest requisicao) {
        UsuarioResponse resposta = UsuarioResponse.de(service.criar(requisicao));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(location).body(resposta);
    }

    @Operation(summary = "Lista usuários com filtros e paginação")
    @GetMapping
    public PaginaResponse<UsuarioResponse> listar(
            @ParameterObject UsuarioFiltro filtro,
            @ParameterObject @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable paginacao) {
        return PaginaResponse.de(service.listar(filtro, Paginacao.validar(paginacao, CAMPOS_ORDENACAO)),
                UsuarioResponse::de);
    }

    @Operation(summary = "Consulta um usuário pelo id")
    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return UsuarioResponse.de(service.buscarPorId(id));
    }

    @Operation(summary = "Atualiza os dados de um usuário")
    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id,
                                     @Validated(Atualizacao.class) @RequestBody UsuarioRequest requisicao) {
        return UsuarioResponse.de(service.atualizar(id, requisicao));
    }

    @Operation(summary = "Ativa um usuário", description = "Operação idempotente.")
    @PatchMapping("/{id}/ativar")
    public UsuarioResponse ativar(@PathVariable Long id) {
        return UsuarioResponse.de(service.ativar(id));
    }

    @Operation(summary = "Inativa um usuário",
            description = "O usuário continua disponível para consulta, mas não pode ser designado a novas obrigações. "
                    + "Operação idempotente.")
    @PatchMapping("/{id}/inativar")
    public UsuarioResponse inativar(@PathVariable Long id) {
        return UsuarioResponse.de(service.inativar(id));
    }
}
