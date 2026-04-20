package com.calycontrol.otica.domain.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLoginRepository extends JpaRepository<AuditLogin, Long> {

    // Últimas N tentativas de um IP (para análise de ataque distribuído)
    List<AuditLogin> findTop20ByIpOrderByCriadoEmDesc(String ip);

    // Todas as falhas de um e-mail em um período (para análise de conta)
    @Query("SELECT a FROM AuditLogin a WHERE a.emailTentativa = :email " +
           "AND a.sucesso = false AND a.criadoEm >= :desde ORDER BY a.criadoEm DESC")
    List<AuditLogin> findFalhasRecentes(@Param("email") String email,
                                        @Param("desde") LocalDateTime desde);

    /**
     * LGPD Art. 15/16 — retenção limitada ao necessário para a finalidade.
     * Remove registros anteriores à data informada.
     */
    long deleteByCriadoEmBefore(LocalDateTime limite);
}
