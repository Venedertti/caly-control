package com.calycontrol.otica.domain.receita;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReceitaOpticaRepository extends JpaRepository<ReceitaOptica, Long> {

    @Query("SELECT r FROM ReceitaOptica r JOIN FETCH r.cliente JOIN FETCH r.medico " +
           "WHERE r.cliente.id = :clienteId ORDER BY r.dataEmissao DESC")
    List<ReceitaOptica> findByClienteId(@Param("clienteId") Long clienteId);

    @Query("SELECT r FROM ReceitaOptica r JOIN FETCH r.cliente JOIN FETCH r.medico " +
           "ORDER BY r.dataEmissao DESC")
    List<ReceitaOptica> findAllComDetalhes();
}
