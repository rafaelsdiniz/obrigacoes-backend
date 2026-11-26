package br.com.obrigacoes.obrigacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoStatusObrigacaoRepository extends JpaRepository<HistoricoStatusObrigacao, Long> {

    List<HistoricoStatusObrigacao> findByObrigacaoIdOrderByAlteradoEmAscIdAsc(Long obrigacaoId);
}
