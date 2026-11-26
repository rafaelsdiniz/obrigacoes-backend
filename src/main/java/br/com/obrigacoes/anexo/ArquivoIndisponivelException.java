package br.com.obrigacoes.anexo;

import br.com.obrigacoes.shared.exception.ApiException;
import br.com.obrigacoes.shared.exception.CodigoErro;
import org.springframework.http.HttpStatus;

public class ArquivoIndisponivelException extends ApiException {

    public ArquivoIndisponivelException() {
        super("O arquivo deste anexo não está disponível no armazenamento.");
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getTitulo() {
        return "Arquivo indisponível";
    }

    @Override
    public CodigoErro getCodigo() {
        return CodigoErro.RECURSO_NAO_ENCONTRADO;
    }
}
