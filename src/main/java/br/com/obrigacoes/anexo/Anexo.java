package br.com.obrigacoes.anexo;

import br.com.obrigacoes.obrigacao.Obrigacao;
import br.com.obrigacoes.shared.domain.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "anexo")
public class Anexo extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "obrigacao_id", nullable = false, updatable = false)
    private Obrigacao obrigacao;

    @Column(name = "nome_original", nullable = false, length = NomesDeArquivo.TAMANHO_MAXIMO, updatable = false)
    private String nomeOriginal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private TipoArquivo tipo;

    @Column(nullable = false, updatable = false)
    private long tamanho;

    @Column(name = "chave_armazenamento", nullable = false, length = 100, unique = true, updatable = false)
    private String chaveArmazenamento;

    @Column(name = "hash_sha256", nullable = false, length = 64, updatable = false)
    private String hashSha256;

    protected Anexo() {
    }

    Anexo(Obrigacao obrigacao, String nomeOriginal, TipoArquivo tipo, ArquivoArmazenado arquivo) {
        this.obrigacao = Objects.requireNonNull(obrigacao, "obrigacao é obrigatória");
        this.nomeOriginal = Objects.requireNonNull(nomeOriginal, "nomeOriginal é obrigatório");
        this.tipo = Objects.requireNonNull(tipo, "tipo é obrigatório");
        this.tamanho = arquivo.tamanho();
        this.chaveArmazenamento = arquivo.chave();
        this.hashSha256 = arquivo.hashSha256();
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public TipoArquivo getTipo() {
        return tipo;
    }

    public long getTamanho() {
        return tamanho;
    }

    String getChaveArmazenamento() {
        return chaveArmazenamento;
    }

    public String getHashSha256() {
        return hashSha256;
    }
}
