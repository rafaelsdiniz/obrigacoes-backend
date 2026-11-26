package br.com.obrigacoes.obrigacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "historico_status_obrigacao")
public class HistoricoStatusObrigacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "obrigacao_id", nullable = false, updatable = false)
    private Obrigacao obrigacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 15, updatable = false)
    private StatusObrigacao statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 15, updatable = false)
    private StatusObrigacao statusNovo;

    @Column(name = "alterado_em", nullable = false, updatable = false)
    private Instant alteradoEm;

    protected HistoricoStatusObrigacao() {
    }

    HistoricoStatusObrigacao(Obrigacao obrigacao, StatusObrigacao statusAnterior, StatusObrigacao statusNovo, Instant alteradoEm) {
        this.obrigacao = obrigacao;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.alteradoEm = alteradoEm.truncatedTo(ChronoUnit.MICROS);
    }

    public StatusObrigacao getStatusAnterior() {
        return statusAnterior;
    }

    public StatusObrigacao getStatusNovo() {
        return statusNovo;
    }

    public Instant getAlteradoEm() {
        return alteradoEm;
    }
}
