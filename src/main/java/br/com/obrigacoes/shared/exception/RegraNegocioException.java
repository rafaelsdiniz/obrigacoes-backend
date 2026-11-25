package br.com.obrigacoes.shared.exception;

import org.springframework.http.HttpStatus;

public class RegraNegocioException extends ApiException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNPROCESSABLE_CONTENT;
    }

    @Override
    public String getTitulo() {
        return "Regra de negócio violada";
    }

    @Override
    public CodigoErro getCodigo() {
        return CodigoErro.REGRA_NEGOCIO;
    }
}
