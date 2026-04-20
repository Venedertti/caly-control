package com.calycontrol.otica.domain.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditRetentionJobTest {

    @Mock
    private AuditLoginRepository repository;

    @InjectMocks
    private AuditRetentionJob job;

    @Test
    @DisplayName("deve remover registros com mais de 90 dias")
    void deveRemoverRegistrosAntigos() {
        when(repository.deleteByCriadoEmBefore(any())).thenReturn(7L);

        job.purgarRegistrosAntigos();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repository).deleteByCriadoEmBefore(captor.capture());

        LocalDateTime esperado = LocalDateTime.now().minusDays(90);
        long diferencaSegundos = Math.abs(ChronoUnit.SECONDS.between(captor.getValue(), esperado));
        assertThat(diferencaSegundos).isLessThan(5);
    }

    @Test
    @DisplayName("deve executar sem erro quando não há registros antigos")
    void deveExecutar_quandoNenhumRegistro() {
        when(repository.deleteByCriadoEmBefore(any())).thenReturn(0L);

        job.purgarRegistrosAntigos();

        verify(repository).deleteByCriadoEmBefore(any());
    }
}
