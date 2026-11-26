package br.com.obrigacoes.anexo;

import br.com.obrigacoes.anexo.dto.AnexoResponse;
import br.com.obrigacoes.shared.exception.ParametroInvalidoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "Anexos", description = "Documentos vinculados a cada obrigação")
@RestController
@RequestMapping("/api/v1/obrigacoes/{obrigacaoId}/anexos")
public class AnexoController {

    private static final String CAMPO_ARQUIVO = "arquivo";
    private static final String SEM_DETECCAO_DE_TIPO = "nosniff";
    private static final String CONTEUDO_ISOLADO = "sandbox";

    private final AnexoService service;

    public AnexoController(AnexoService service) {
        this.service = service;
    }

    @Operation(summary = "Anexa um arquivo à obrigação",
            description = "Um arquivo por requisição. Tipos aceitos: PDF, PNG, JPEG, XML, CSV, XLSX e DOCX, "
                    + "conferidos pelo conteúdo do arquivo.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnexoResponse> anexar(@PathVariable Long obrigacaoId,
                                                @RequestParam(CAMPO_ARQUIVO) MultipartFile arquivo,
                                                @Parameter(hidden = true) MultipartHttpServletRequest requisicao) {
        if (requisicao.getFiles(CAMPO_ARQUIVO).size() > 1) {
            throw new ParametroInvalidoException("Envie um único arquivo por requisição.");
        }
        AnexoResponse resposta = AnexoResponse.de(service.anexar(obrigacaoId, arquivo));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(location).body(resposta);
    }

    @Operation(summary = "Lista os anexos da obrigação, do mais recente ao mais antigo")
    @GetMapping
    public List<AnexoResponse> listar(@PathVariable Long obrigacaoId) {
        return service.listar(obrigacaoId).stream().map(AnexoResponse::de).toList();
    }

    @Operation(summary = "Baixa o arquivo de um anexo com o nome original")
    @GetMapping("/{anexoId}")
    public ResponseEntity<Resource> baixar(@PathVariable Long obrigacaoId, @PathVariable Long anexoId) {
        AnexoService.ConteudoAnexo download = service.baixar(obrigacaoId, anexoId);
        Anexo anexo = download.anexo();
        ContentDisposition disposicao = ContentDisposition.attachment()
                .filename(anexo.getNomeOriginal(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(anexo.getTipo().getTipoConteudo()))
                .contentLength(anexo.getTamanho())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicao.toString())
                .header("X-Content-Type-Options", SEM_DETECCAO_DE_TIPO)
                .header("Content-Security-Policy", CONTEUDO_ISOLADO)
                .body(download.conteudo());
    }

    @Operation(summary = "Exclui um anexo", description = "Não é permitido excluir anexos de obrigações concluídas.")
    @DeleteMapping("/{anexoId}")
    public ResponseEntity<Void> excluir(@PathVariable Long obrigacaoId, @PathVariable Long anexoId) {
        service.excluir(obrigacaoId, anexoId);
        return ResponseEntity.noContent().build();
    }
}
