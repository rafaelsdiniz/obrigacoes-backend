package br.com.obrigacoes.obrigacao;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ObrigacaoRepository extends JpaRepository<Obrigacao, Long>, JpaSpecificationExecutor<Obrigacao> {

    @Override
    @EntityGraph(attributePaths = {"empresa", "responsavel"})
    Optional<Obrigacao> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"empresa", "responsavel"})
    Page<Obrigacao> findAll(Specification<Obrigacao> especificacao, Pageable paginacao);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Obrigacao o where o.id = :id")
    Optional<Obrigacao> buscarComBloqueio(@Param("id") Long id);
}
