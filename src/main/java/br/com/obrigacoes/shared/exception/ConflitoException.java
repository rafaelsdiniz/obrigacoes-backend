package br.com.obrigacoes.shared.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ConflitoException extends ApiException {

    private final List<ErroCampo> erros;

    public ConflitoException(String mensagem) {
        super(mensagem);
        this.erros = List.of();
    }

    public ConflitoException(String campo, String mensagem) {
        super(mensagem);
        this.erros = List.of(new ErroCampo(campo, mensagem));
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String getTitulo() {
        return "Conflito de dados";
    }

    @Override
    public CodigoErro getCodigo() {
        return CodigoErro.CONFLITO;
    }

    @Override
    public List<ErroCampo> getErros() {
        return erros;
    }
}
