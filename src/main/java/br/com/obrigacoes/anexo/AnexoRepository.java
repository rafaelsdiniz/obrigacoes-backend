package br.com.obrigacoes.anexo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface AnexoRepository extends JpaRepository<Anexo, Long> {

    List<Anexo> findByObrigacaoIdOrderByCriadoEmDescIdDesc(Long obrigacaoId);

    Optional<Anexo> findByIdAndObrigacaoId(Long id, Long obrigacaoId);
}
