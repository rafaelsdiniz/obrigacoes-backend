package br.com.obrigacoes.shared.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public abstract class ApiException extends RuntimeException {

    protected ApiException(String mensagem) {
        super(mensagem);
    }

    public abstract HttpStatus getStatus();

    public abstract String getTitulo();

    public abstract CodigoErro getCodigo();

    public List<ErroCampo> getErros() {
        return List.of();
    }
}
