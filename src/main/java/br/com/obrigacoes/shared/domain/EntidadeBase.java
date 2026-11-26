package br.com.obrigacoes.shared.domain;

import br.com.obrigacoes.shared.exception.ConflitoException;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EntidadeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @LastModifiedDate
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Version
    @Column(nullable = false)
    private Long versao;

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
