package com.calycontrol.otica.domain.usuario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    @Test
    @DisplayName("isContaBloqueada deve retornar false quando bloqueadoAte é null")
    void isContaBloqueada_quandoNull_retornaFalse() {
        Usuario u = new Usuario();
        u.setBloqueadoAte(null);
        assertThat(u.isContaBloqueada()).isFalse();
    }

    @Test
    @DisplayName("isContaBloqueada deve retornar true quando bloqueadoAte está no futuro")
    void isContaBloqueada_quandoNoFuturo_retornaTrue() {
        Usuario u = new Usuario();
        u.setBloqueadoAte(LocalDateTime.now().plusMinutes(10));
        assertThat(u.isContaBloqueada()).isTrue();
    }

    @Test
    @DisplayName("isContaBloqueada deve retornar false quando bloqueadoAte está no passado")
    void isContaBloqueada_quandoNoPassado_retornaFalse() {
        Usuario u = new Usuario();
        u.setBloqueadoAte(LocalDateTime.now().minusMinutes(1));
        assertThat(u.isContaBloqueada()).isFalse();
    }
}
