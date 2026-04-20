package com.calycontrol.otica.domain.fornecedor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    List<Fornecedor> findByAtivoTrueOrderByRazaoSocial();

    Optional<Fornecedor> findByCnpj(String cnpj);
}
