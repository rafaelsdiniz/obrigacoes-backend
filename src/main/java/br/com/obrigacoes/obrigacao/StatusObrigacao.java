package br.com.obrigacoes.obrigacao;

import java.util.Optional;

public enum StatusObrigacao {
    PENDENTE("Pendente"),
    EM_ANDAMENTO("Em andamento"),
    CONCLUIDA("Concluída");

    private final String descricao;

    StatusObrigacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public Optional<StatusObrigacao> proximo() {
        return switch (this) {
            case PENDENTE -> Optional.of(EM_ANDAMENTO);
            case EM_ANDAMENTO -> Optional.of(CONCLUIDA);
            case CONCLUIDA -> Optional.empty();
        };
    }

    public boolean podeAvancarPara(StatusObrigacao destino) {
        return proximo().filter(destino::equals).isPresent();
    }

    public boolean isFinal() {
        return proximo().isEmpty();
    }
}
