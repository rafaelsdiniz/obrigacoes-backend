package br.com.obrigacoes.empresa;

import br.com.obrigacoes.shared.domain.EntidadeBase;
import br.com.obrigacoes.shared.domain.Situacao;
import br.com.obrigacoes.shared.domain.Textos;
import br.com.obrigacoes.shared.validation.Cnpjs;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "empresa")
public class Empresa extends EntidadeBase {

    @Column(name = "razao_social", nullable = false, length = 150)
    private String razaoSocial;

    @Column(name = "nome_fantasia", length = 150)
    private String nomeFantasia;

    @Column(nullable = false, length = 14, unique = true)
    private String cnpj;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Situacao situacao;

    protected Empresa() {
    }

    public Empresa(String razaoSocial, String nomeFantasia, String cnpj, String email, String telefone) {
        atualizarDados(razaoSocial, nomeFantasia, cnpj, email, telefone);
        this.situacao = Situacao.ATIVO;
    }

    public void atualizarDados(String razaoSocial, String nomeFantasia, String cnpj, String email, String telefone) {
        this.razaoSocial = Objects.requireNonNull(Textos.limparOuNulo(razaoSocial), "razaoSocial é obrigatória");
        this.cnpj = Objects.requireNonNull(Cnpjs.normalizar(cnpj), "cnpj é obrigatório");
        this.nomeFantasia = Textos.limparOuNulo(nomeFantasia);
        this.email = Textos.normalizarEmail(email);
        this.telefone = Textos.limparOuNulo(telefone);
    }

    public void ativar() {
        this.situacao = Situacao.ATIVO;
    }

    public void inativar() {
        this.situacao = Situacao.INATIVO;
    }

    public boolean isAtiva() {
        return situacao == Situacao.ATIVO;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public Situacao getSituacao() {
        return situacao;
    }
}
