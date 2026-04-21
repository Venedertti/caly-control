package com.calycontrol.otica.domain.receita;

import com.calycontrol.otica.domain.cliente.Cliente;
import com.calycontrol.otica.domain.medico.Medico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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

    @Test
    @DisplayName("deve aceitar e retornar todos os campos ópticos via setters e getters")
    void camposOpticos_devemSerAcessiveis() {
        ReceitaOptica r = new ReceitaOptica();

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        Medico medico = new Medico();
        medico.setId(2L);
        LocalDate emissao = LocalDate.now().minusDays(10);
        LocalDate validade = LocalDate.now().plusYears(1);

        r.setId(5L);
        r.setCliente(cliente);
        r.setMedico(medico);
        r.setDataEmissao(emissao);
        r.setValidade(validade);
        r.setOdEsf(new BigDecimal("-2.25"));
        r.setOdCil(new BigDecimal("-0.50"));
        r.setOdEixo(180);
        r.setOeEsf(new BigDecimal("-1.75"));
        r.setOeCil(new BigDecimal("-0.25"));
        r.setOeEixo(175);
        r.setAdicao(new BigDecimal("2.00"));
        r.setDpOd(new BigDecimal("32.0"));
        r.setDpOe(new BigDecimal("31.5"));
        r.setObservacoes("Usar somente para leitura");
        r.setBaseLegal("ART_11_II_C");

        assertThat(r.getId()).isEqualTo(5L);
        assertThat(r.getCliente()).isSameAs(cliente);
        assertThat(r.getMedico()).isSameAs(medico);
        assertThat(r.getDataEmissao()).isEqualTo(emissao);
        assertThat(r.getValidade()).isEqualTo(validade);
        assertThat(r.getOdEsf()).isEqualByComparingTo(new BigDecimal("-2.25"));
        assertThat(r.getOdCil()).isEqualByComparingTo(new BigDecimal("-0.50"));
        assertThat(r.getOdEixo()).isEqualTo(180);
        assertThat(r.getOeEsf()).isEqualByComparingTo(new BigDecimal("-1.75"));
        assertThat(r.getOeCil()).isEqualByComparingTo(new BigDecimal("-0.25"));
        assertThat(r.getOeEixo()).isEqualTo(175);
        assertThat(r.getAdicao()).isEqualByComparingTo(new BigDecimal("2.00"));
        assertThat(r.getDpOd()).isEqualByComparingTo(new BigDecimal("32.0"));
        assertThat(r.getDpOe()).isEqualByComparingTo(new BigDecimal("31.5"));
        assertThat(r.getObservacoes()).isEqualTo("Usar somente para leitura");
        assertThat(r.getBaseLegal()).isEqualTo("ART_11_II_C");
    }
}
