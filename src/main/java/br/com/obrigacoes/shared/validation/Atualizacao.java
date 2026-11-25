package br.com.obrigacoes.shared.validation;

import jakarta.validation.groups.Default;

/**
 * Grupo de validação das requisições de atualização. Estende {@link Default}
 * para que as regras comuns continuem valendo, somando as exclusivas da edição.
 */
public interface Atualizacao extends Default {
}
