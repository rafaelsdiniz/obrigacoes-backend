package br.com.obrigacoes.anexo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
class LimpezaDeArquivos {

    private static final Logger log = LoggerFactory.getLogger(LimpezaDeArquivos.class);

    private final ArmazenamentoArquivos armazenamento;

    LimpezaDeArquivos(ArmazenamentoArquivos armazenamento) {
        this.armazenamento = armazenamento;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoExcluirAnexo(AnexoExcluido evento) {
        try {
            armazenamento.remover(evento.chave());
        } catch (RuntimeException e) {
            log.warn("Não foi possível remover o arquivo {} do armazenamento", evento.chave(), e);
        }
    }

    record AnexoExcluido(String chave) {
    }
}
