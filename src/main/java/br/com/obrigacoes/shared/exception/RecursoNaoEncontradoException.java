package br.com.obrigacoes.shared.exception;

import org.springframework.http.HttpStatus;

public class RecursoNaoEncontradoException extends ApiException {

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super("%s com id %s não foi encontrado(a).".formatted(recurso, id));
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getTitulo() {
        return "Recurso não encontrado";
    }

    @Override
    public CodigoErro getCodigo() {
        return CodigoErro.RECURSO_NAO_ENCONTRADO;
    }
}
