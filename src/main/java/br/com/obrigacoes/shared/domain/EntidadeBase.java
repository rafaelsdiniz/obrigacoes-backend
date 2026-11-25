package br.com.obrigacoes.shared.domain;

import br.com.obrigacoes.shared.exception.ConflitoException;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@MappedSuperclass
public abstract class EntidadeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Version
    @Column(nullable = false)
    private Long versao;

    @PrePersist
    protected void aoCriar() {
        criadoEm = agora();
        atualizadoEm = criadoEm;
    }

    @PreUpdate
    protected void aoAtualizar() {
        atualizadoEm = agora();
    }

    private static Instant agora() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public void verificarVersao(Long versaoInformada) {
        if (!Objects.equals(versao, versaoInformada)) {
            throw new ConflitoException(
                    "O registro foi alterado por outra operação. Recarregue os dados e tente novamente.");
        }
    }

    public Long getId() {
        return id;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public Long getVersao() {
        return versao;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof EntidadeBase entidade) || classeReal(this) != classeReal(outro)) {
            return false;
        }
        return getId() != null && getId().equals(entidade.getId());
    }

    @Override
    public int hashCode() {
        return classeReal(this).hashCode();
    }

    private static Class<?> classeReal(Object objeto) {
        return objeto instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : objeto.getClass();
    }
}
