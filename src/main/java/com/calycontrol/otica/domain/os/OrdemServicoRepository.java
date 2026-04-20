package com.calycontrol.otica.domain.os;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    boolean existsByNumeroOs(String numeroOs);

    @Query("SELECT o FROM OrdemServico o JOIN FETCH o.cliente JOIN FETCH o.usuario " +
           "ORDER BY o.dataAbertura DESC, o.id DESC")
    List<OrdemServico> findAllComDetalhes();

    @Query("SELECT COUNT(o) FROM OrdemServico o WHERE o.status = :status")
    long countByStatus(@Param("status") StatusOS status);

    @Query("SELECT o FROM OrdemServico o JOIN FETCH o.cliente " +
           "WHERE o.status IN :statuses ORDER BY o.dataAbertura DESC")
    List<OrdemServico> findByStatusIn(@Param("statuses") List<StatusOS> statuses);

    List<OrdemServico> findTop10ByOrderByIdDesc();
}
