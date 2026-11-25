package br.com.obrigacoes.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Indica que a operação conflita com o estado atual dos dados,
 * como um CNPJ ou e-mail já cadastrado.
 */
public class ConflitoException extends ApiException {

    public ConflitoException(String mensagem) {
        super(mensagem);
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
}
