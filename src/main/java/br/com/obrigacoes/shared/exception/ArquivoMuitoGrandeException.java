package br.com.obrigacoes.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;

public class ArquivoMuitoGrandeException extends ApiException {

    public ArquivoMuitoGrandeException(DataSize tamanhoMaximo) {
        super("O arquivo excede o tamanho máximo de %s.".formatted(Tamanhos.descrever(tamanhoMaximo)));
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONTENT_TOO_LARGE;
    }

    @Override
    public String getTitulo() {
        return "Arquivo muito grande";
    }

    @Override
    public CodigoErro getCodigo() {
        return CodigoErro.ARQUIVO_INVALIDO;
    }
}
