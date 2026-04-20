package com.calycontrol.otica.domain.produto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByAtivoTrueOrderByDescricao();

    Optional<Produto> findByCodigo(String codigo);

    List<Produto> findByAtivoTrueAndTipo(TipoProduto tipo);

    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND " +
           "(LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.codigo) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.marca) LIKE LOWER(CONCAT('%', :termo, '%')))")
    List<Produto> buscar(@Param("termo") String termo);

    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND p.estoqueMinimo > 0 AND p.estoqueAtual <= p.estoqueMinimo")
    List<Produto> findEstoqueBaixo();
}
