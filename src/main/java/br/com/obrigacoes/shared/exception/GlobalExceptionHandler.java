package br.com.obrigacoes.shared.exception;

import jakarta.servlet.MultipartConfigElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.unit.DataSize;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import tools.jackson.databind.exc.MismatchedInputException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String PROP_CODIGO = "codigo";
    private static final String PROP_TIMESTAMP = "timestamp";
    private static final String PROP_ERROS = "erros";

    private final DataSize tamanhoMaximoArquivo;

    public GlobalExceptionHandler(MultipartConfigElement configuracaoMultipart) {
        this.tamanhoMaximoArquivo = DataSize.ofBytes(configuracaoMultipart.getMaxFileSize());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetail> handleApiException(ApiException ex) {
        return responder(problemaDe(ex));
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, PessimisticLockingFailureException.class})
    public ResponseEntity<ProblemDetail> handleConcorrencia(ConcurrencyFailureException ex) {
        log.debug("Conflito de concorrência detectado", ex);
        return responder(problema(HttpStatus.CONFLICT, "Conflito de dados",
                "O registro foi alterado por outra operação. Recarregue os dados e tente novamente.",
                CodigoErro.CONFLITO));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex) {
        String restricao = ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException violacao
                ? violacao.getConstraintName() : "desconhecida";
        log.warn("Violação de integridade dos dados (restrição: {})", restricao);
        return responder(problema(HttpStatus.CONFLICT, "Conflito de dados",
                "A operação viola uma restrição de integridade dos dados.", CodigoErro.CONFLITO));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleInesperada(Exception ex) {
        log.error("Erro inesperado ao processar a requisição", ex);
        return responder(problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", CodigoErro.ERRO_INTERNO));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        var resultado = ex.getBindingResult();
        List<ErroCampo> erros = Stream.concat(
                resultado.getFieldErrors().stream()
                        .map(erro -> new ErroCampo(erro.getField(),
                                erro.isBindingFailure() ? "Valor inválido." : erro.getDefaultMessage())),
                resultado.getGlobalErrors().stream()
                        .map(erro -> new ErroCampo(erro.getObjectName(), erro.getDefaultMessage()))
        ).toList();
        return handleExceptionInternal(ex, problemaDeValidacao(erros), headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers,
                                                                            HttpStatusCode status,
                                                                            WebRequest request) {
        List<ErroCampo> erros = ex.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(erro -> new ErroCampo(resultado.getMethodParameter().getParameterName(),
                                erro.getDefaultMessage())))
                .toList();
        return handleExceptionInternal(ex, problemaDeValidacao(erros), headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail problema = campoComValorInvalido(ex)
                .map(campo -> problemaDeValidacao(List.of(new ErroCampo(campo, "Valor inválido."))))
                .orElseGet(() -> problema(HttpStatus.BAD_REQUEST, "Requisição inválida",
                        "O corpo da requisição está ausente ou malformado.", CodigoErro.REQUISICAO_INVALIDA));
        return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
    }

    private static Optional<String> campoComValorInvalido(Throwable ex) {
        for (Throwable causa = ex; causa != null; causa = causa.getCause()) {
            if (causa instanceof MismatchedInputException erro && !erro.getPath().isEmpty()) {
                return Optional.of(erro.getPath().stream()
                        .map(ref -> ref.getPropertyName() != null ? ref.getPropertyName() : "[" + ref.getIndex() + "]")
                        .collect(Collectors.joining(".")));
            }
        }
        return Optional.empty();
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
                                                        HttpHeaders headers,
                                                        HttpStatusCode status,
                                                        WebRequest request) {
        String detalhe = "O valor '%s' é inválido para o parâmetro '%s'.".formatted(ex.getValue(), ex.getPropertyName());
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Requisição inválida", detalhe,
                CodigoErro.REQUISICAO_INVALIDA);
        return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          WebRequest request) {
        return handleExceptionInternal(ex, parametroObrigatorioAusente(ex.getParameterName()), headers,
                HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex,
                                                                     HttpHeaders headers,
                                                                     HttpStatusCode status,
                                                                     WebRequest request) {
        return handleExceptionInternal(ex, parametroObrigatorioAusente(ex.getRequestPartName()), headers,
                HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          WebRequest request) {
        return handleExceptionInternal(ex, problemaDe(new ArquivoMuitoGrandeException(tamanhoMaximoArquivo)), headers,
                HttpStatus.CONTENT_TOO_LARGE, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        Object corpo = body == null && ex instanceof ErrorResponse errorResponse ? errorResponse.getBody() : body;
        if (corpo instanceof ProblemDetail problema && !possuiCodigo(problema)) {
            traduzir(problema, statusCode);
        }
        return super.handleExceptionInternal(ex, corpo, headers, statusCode, request);
    }

    private static ProblemDetail problema(HttpStatusCode status, String titulo, String detalhe, CodigoErro codigo) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        problema.setProperty(PROP_CODIGO, codigo);
        problema.setProperty(PROP_TIMESTAMP, Instant.now());
        return problema;
    }

    private static ProblemDetail problemaDe(ApiException ex) {
        ProblemDetail problema = problema(ex.getStatus(), ex.getTitulo(), ex.getMessage(), ex.getCodigo());
        if (!ex.getErros().isEmpty()) {
            problema.setProperty(PROP_ERROS, ex.getErros());
        }
        return problema;
    }

    private static ProblemDetail parametroObrigatorioAusente(String nome) {
        return problema(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "O parâmetro obrigatório '%s' não foi informado.".formatted(nome), CodigoErro.REQUISICAO_INVALIDA);
    }

    private static ProblemDetail problemaDeValidacao(List<ErroCampo> erros) {
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos estão inválidos.", CodigoErro.VALIDACAO);
        problema.setProperty(PROP_ERROS, erros);
        return problema;
    }

    private static void traduzir(ProblemDetail problema, HttpStatusCode status) {
        switch (status.value()) {
            case 404 -> definir(problema, "Recurso não encontrado", "O recurso solicitado não existe.");
            case 405 -> definir(problema, "Método não permitido", "O método HTTP não é suportado para este recurso.");
            case 406 -> definir(problema, "Formato não aceito", "O formato de resposta solicitado não é suportado.");
            case 415 -> definir(problema, "Tipo de mídia não suportado",
                    "O tipo de conteúdo da requisição não é suportado.");
            default -> problema.setTitle(status.is5xxServerError() ? "Erro interno" : "Requisição inválida");
        }
        problema.setProperty(PROP_CODIGO, status.is5xxServerError() ? CodigoErro.ERRO_INTERNO : CodigoErro.REQUISICAO_INVALIDA);
        problema.setProperty(PROP_TIMESTAMP, Instant.now());
    }

    private static void definir(ProblemDetail problema, String titulo, String detalhe) {
        problema.setTitle(titulo);
        problema.setDetail(detalhe);
    }

    private static boolean possuiCodigo(ProblemDetail problema) {
        return problema.getProperties() != null && problema.getProperties().containsKey(PROP_CODIGO);
    }

    private static ResponseEntity<ProblemDetail> responder(ProblemDetail problema) {
        return ResponseEntity.status(problema.getStatus()).body(problema);
    }
}
