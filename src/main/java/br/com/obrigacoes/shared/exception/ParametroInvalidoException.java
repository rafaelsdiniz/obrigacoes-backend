package br.com.obrigacoes.shared.exception;

import org.springframework.http.HttpStatus;

public class ParametroInvalidoException extends ApiException {

    public ParametroInvalidoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getTitulo() {
        return "Requisição inválida";
    }

    @Override
    public CodigoErro getCodigo() {
        return CodigoErro.REQUISICAO_INVALIDA;
    }
}
