package br.com.obrigacoes.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Indica um parâmetro de consulta não suportado, como uma ordenação por campo não permitido.
 */
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
