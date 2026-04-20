package com.calycontrol.otica.domain.venda;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    Optional<Venda> findByNumeroVenda(String numeroVenda);

    boolean existsByNumeroVenda(String numeroVenda);

    List<Venda> findTop20ByOrderByDataVendaDesc();
}
