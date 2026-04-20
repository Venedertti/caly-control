package com.calycontrol.otica.domain.receita;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ReceitaOpticaTest {

    @Test
    @DisplayName("isExpirada deve retornar false quando validade é futura")
    void isExpirada_quandoValidade_futura() {
        ReceitaOptica r = new ReceitaOptica();
        r.setValidade(LocalDate.now().plusDays(1));
        assertThat(r.isExpirada()).isFalse();
    }

    @Test
    @DisplayName("isExpirada deve retornar true quando validade já passou")
    void isExpirada_quandoValidade_passada() {
        ReceitaOptica r = new ReceitaOptica();
        r.setValidade(LocalDate.now().minusDays(1));
        assertThat(r.isExpirada()).isTrue();
    }

    @Test
    @DisplayName("isExpirada deve retornar false quando validade é hoje")
    void isExpirada_quandoValidade_hoje() {
        ReceitaOptica r = new ReceitaOptica();
        r.setValidade(LocalDate.now());
        assertThat(r.isExpirada()).isFalse();
    }

    @Test
    @DisplayName("baseLegal deve ter valor padrão ART_11_II_C")
    void baseLegal_deveSerPadraoArt11() {
        ReceitaOptica r = new ReceitaOptica();
        assertThat(r.getBaseLegal()).isEqualTo("ART_11_II_C");
    }
}
