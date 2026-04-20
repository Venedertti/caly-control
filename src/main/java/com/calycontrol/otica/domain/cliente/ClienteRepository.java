package com.calycontrol.otica.domain.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByAtivoTrueOrderByNome();

    Optional<Cliente> findByCpf(String cpf);

    @Query("SELECT c FROM Cliente c WHERE c.ativo = true AND " +
           "(LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR c.cpf LIKE CONCAT('%', :termo, '%') OR c.telefone LIKE CONCAT('%', :termo, '%'))")
    List<Cliente> buscar(@Param("termo") String termo);
}
