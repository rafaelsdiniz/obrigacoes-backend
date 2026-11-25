package br.com.obrigacoes.shared.exception;

/**
 * Erro de validação associado a um campo da requisição.
 */
public record ErroCampo(String campo, String mensagem) {
}
