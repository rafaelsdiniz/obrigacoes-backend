package br.com.obrigacoes.shared.domain;

/**
 * Situação cadastral de empresas e usuários.
 * Registros inativos continuam consultáveis, mas não podem receber novas atribuições.
 */
public enum Situacao {
    ATIVO,
    INATIVO
}
