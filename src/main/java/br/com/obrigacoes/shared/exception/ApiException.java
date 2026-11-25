package br.com.obrigacoes.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Base das exceções de negócio da aplicação.
 * <p>
 * Cada subclasse define o status HTTP e o código que o cliente recebe,
 * permitindo que o {@link GlobalExceptionHandler} trate todas de forma uniforme.
 */
public abstract class ApiException extends RuntimeException {

    protected ApiException(String mensagem) {
        super(mensagem);
    }

    public abstract HttpStatus getStatus();

    public abstract String getTitulo();

    public abstract CodigoErro getCodigo();
}
