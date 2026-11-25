package br.com.obrigacoes.obrigacao;

import br.com.obrigacoes.empresa.Empresa;
import br.com.obrigacoes.shared.domain.EntidadeBase;
import br.com.obrigacoes.shared.domain.Textos;
import br.com.obrigacoes.shared.exception.RegraNegocioException;
import br.com.obrigacoes.shared.persistence.YearMonthConverter;
import br.com.obrigacoes.usuario.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "obrigacao")
public class Obrigacao extends EntidadeBase {

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoObrigacao tipo;

    @Convert(converter = YearMonthConverter.class)
    @Column(length = 7)
    private YearMonth competencia;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusObrigacao status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    @OneToMany(mappedBy = "obrigacao", cascade = CascadeType.PERSIST)
    private final List<HistoricoStatusObrigacao> historico = new ArrayList<>();

    protected Obrigacao() {
    }

    public Obrigacao(String titulo, String descricao, TipoObrigacao tipo, YearMonth competencia,
                     LocalDate dataVencimento, Empresa empresa, Usuario responsavel, Instant agora) {
        definirDados(titulo, descricao, tipo, competencia, dataVencimento, empresa, responsavel);
        this.status = StatusObrigacao.PENDENTE;
        historico.add(new HistoricoStatusObrigacao(this, null, StatusObrigacao.PENDENTE, agora));
    }

    public void atualizarDados(String titulo, String descricao, TipoObrigacao tipo, YearMonth competencia,
                               LocalDate dataVencimento, Empresa empresa, Usuario responsavel) {
        garantirQueNaoEstaConcluida();
        definirDados(titulo, descricao, tipo, competencia, dataVencimento, empresa, responsavel);
    }

    public void alterarStatus(StatusObrigacao novoStatus, Instant agora) {
        Objects.requireNonNull(novoStatus, "novoStatus é obrigatório");
        garantirQueNaoEstaConcluida();
        if (novoStatus == status) {
            throw new RegraNegocioException("A obrigação já está com o status %s.".formatted(status.getDescricao()));
        }
        if (!status.podeAvancarPara(novoStatus)) {
            throw new RegraNegocioException("Não é possível alterar o status de %s para %s. O próximo status permitido é %s."
                    .formatted(status.getDescricao(), novoStatus.getDescricao(), status.proximo().orElseThrow().getDescricao()));
        }
        historico.add(new HistoricoStatusObrigacao(this, status, novoStatus, agora));
        this.status = novoStatus;
    }

    public boolean isAtrasada(LocalDate hoje) {
        return !status.isFinal() && dataVencimento.isBefore(hoje);
    }

    private void garantirQueNaoEstaConcluida() {
        if (status != null && status.isFinal()) {
            throw new RegraNegocioException("A obrigação já foi concluída e não pode mais ser alterada.");
        }
    }

    private void definirDados(String titulo, String descricao, TipoObrigacao tipo, YearMonth competencia,
                              LocalDate dataVencimento, Empresa empresa, Usuario responsavel) {
        this.titulo = Objects.requireNonNull(Textos.limparOuNulo(titulo), "titulo é obrigatório");
        this.descricao = Textos.limparOuNulo(descricao);
        this.tipo = Objects.requireNonNull(tipo, "tipo é obrigatório");
        this.competencia = competencia;
        this.dataVencimento = Objects.requireNonNull(dataVencimento, "dataVencimento é obrigatória");
        this.empresa = Objects.requireNonNull(empresa, "empresa é obrigatória");
        this.responsavel = Objects.requireNonNull(responsavel, "responsavel é obrigatório");
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public TipoObrigacao getTipo() {
        return tipo;
    }

    public YearMonth getCompetencia() {
        return competencia;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public StatusObrigacao getStatus() {
        return status;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public List<HistoricoStatusObrigacao> getHistorico() {
        return List.copyOf(historico);
    }
}
