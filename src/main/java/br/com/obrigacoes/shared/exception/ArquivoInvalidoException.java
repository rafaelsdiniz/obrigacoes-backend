package br.com.obrigacoes.shared.exception;

import org.springframework.http.HttpStatus;

public class ArquivoInvalidoException extends ApiException {

    public ArquivoInvalidoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getTitulo() {
        return "Arquivo inválido";
    }

    @Override
    public CodigoErro getCodigo() {
        return CodigoErro.ARQUIVO_INVALIDO;
    }
}
