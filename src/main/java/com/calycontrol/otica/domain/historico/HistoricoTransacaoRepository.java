package com.calycontrol.otica.domain.historico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HistoricoTransacaoRepository extends JpaRepository<HistoricoTransacao, Long> {

    List<HistoricoTransacao> findAllByOrderByDataTransacaoDesc();

    Optional<HistoricoTransacao> findByOrigemTabelaAndOrigemId(String origemTabela, Long origemId);

    List<HistoricoTransacao> findByClienteId(Long clienteId);

    /**
     * LGPD Art. 18, IV — limpa os dados pessoais desnormalizados das linhas
     * de histórico associadas ao cliente informado, preservando a integridade
     * financeira/contábil (valores, datas, número de referência).
     */
    @Modifying
    @Query("UPDATE HistoricoTransacao h SET " +
           "h.clienteNome = 'ANONIMIZADO', " +
           "h.clienteCpf = NULL, " +
           "h.clienteTelefone = NULL, " +
           "h.clienteEmail = NULL " +
           "WHERE h.clienteId = :clienteId")
    int anonimizarPorCliente(@Param("clienteId") Long clienteId);
}
