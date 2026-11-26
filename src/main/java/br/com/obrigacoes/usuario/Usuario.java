package br.com.obrigacoes.usuario;

import br.com.obrigacoes.shared.domain.EntidadeBase;
import br.com.obrigacoes.shared.domain.Situacao;
import br.com.obrigacoes.shared.domain.Textos;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "usuario")
public class Usuario extends EntidadeBase {

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(length = 80)
    private String cargo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Situacao situacao;

    protected Usuario() {
    }

    public Usuario(String nome, String email, String cargo) {
        atualizarDados(nome, email, cargo);
        this.situacao = Situacao.ATIVO;
    }

    public void atualizarDados(String nome, String email, String cargo) {
        this.nome = Objects.requireNonNull(Textos.limparOuNulo(nome), "nome é obrigatório");
        this.email = Objects.requireNonNull(Textos.normalizarEmail(email), "email é obrigatório");
        this.cargo = Textos.limparOuNulo(cargo);
    }

    public void ativar() {
        this.situacao = Situacao.ATIVO;
    }

    public void inativar() {
        this.situacao = Situacao.INATIVO;
    }

    public boolean isAtivo() {
        return situacao == Situacao.ATIVO;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getCargo() {
        return cargo;
    }

    public Situacao getSituacao() {
        return situacao;
    }
}
