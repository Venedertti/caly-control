package com.calycontrol.otica.domain.medico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    List<Medico> findByAtivoTrueOrderByNome();

    Optional<Medico> findByCrm(String crm);
}
