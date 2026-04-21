package com.calycontrol.otica.domain.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLoginTest {

    @Test
    @DisplayName("sucesso deve criar registro com sucesso=true e campos preenchidos")
    void sucesso_deveCriarRegistroCorreto() {
        AuditLogin a = AuditLogin.sucesso("admin@caly.com", 1L, "10.0.0.1", "Mozilla/5.0");

        assertThat(a.isSucesso()).isTrue();
        assertThat(a.getEmailTentativa()).isEqualTo("admin@caly.com");
        assertThat(a.getUsuarioId()).isEqualTo(1L);
        assertThat(a.getIp()).isEqualTo("10.0.0.1");
        assertThat(a.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(a.getMotivoFalha()).isNull();
        assertThat(a.getCriadoEm()).isNotNull();
    }

    @Test
    @DisplayName("falha deve criar registro com sucesso=false e motivo preenchido")
    void falha_deveCriarRegistroCorreto() {
        AuditLogin a = AuditLogin.falha("user@caly.com", 2L, "192.168.0.1", "curl/7.0", "BadCredentialsException");

        assertThat(a.isSucesso()).isFalse();
        assertThat(a.getEmailTentativa()).isEqualTo("user@caly.com");
        assertThat(a.getUsuarioId()).isEqualTo(2L);
        assertThat(a.getIp()).isEqualTo("192.168.0.1");
        assertThat(a.getUserAgent()).isEqualTo("curl/7.0");
        assertThat(a.getMotivoFalha()).isEqualTo("BadCredentialsException");
        assertThat(a.getCriadoEm()).isNotNull();
    }

    @Test
    @DisplayName("falha com usuarioId nulo deve funcionar para e-mails não cadastrados")
    void falha_comUsuarioIdNulo_deveFuncionar() {
        AuditLogin a = AuditLogin.falha("ghost@test.com", null, "1.2.3.4", null, "USER_NOT_FOUND");

        assertThat(a.isSucesso()).isFalse();
        assertThat(a.getUsuarioId()).isNull();
        assertThat(a.getUserAgent()).isNull();
        assertThat(a.getMotivoFalha()).isEqualTo("USER_NOT_FOUND");
    }

    @Test
    @DisplayName("getId deve retornar null antes de persistência")
    void getId_antesDePersisteir_retornaNull() {
        AuditLogin a = AuditLogin.sucesso("x@x.com", 1L, "1.1.1.1", "UA");
        assertThat(a.getId()).isNull();
    }
}
