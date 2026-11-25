package br.com.obrigacoes.shared.exception;

/**
 * Códigos estáveis enviados no campo {@code codigo} das respostas de erro,
 * para que o cliente possa reagir a cada situação sem depender do texto da mensagem.
 */
public enum CodigoErro {
    VALIDACAO,
    REQUISICAO_INVALIDA,
    RECURSO_NAO_ENCONTRADO,
    CONFLITO,
    REGRA_NEGOCIO,
    ARQUIVO_INVALIDO,
    ERRO_INTERNO
}
