package com.calycontrol.otica.domain.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Job de retenção do log de autenticação (LGPD Art. 15/16).
 *
 * O audit_login armazena e-mail tentado, IP e User-Agent — dados pessoais
 * necessários apenas para detecção de fraude/invasão no curto prazo.
 * Mantê-los além do necessário viola o princípio da necessidade (Art. 6º, III).
 *
 * Retenção padrão: 90 dias. Executa diariamente às 03:00.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditRetentionJob {

    private static final int DIAS_RETENCAO = 90;

    private final AuditLoginRepository repository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgarRegistrosAntigos() {
        LocalDateTime limite = LocalDateTime.now().minusDays(DIAS_RETENCAO);
        long removidos = repository.deleteByCriadoEmBefore(limite);
        if (removidos > 0) {
            log.info("LGPD/audit_login: {} registros anteriores a {} foram removidos.",
                    removidos, limite);
        }
    }
}
