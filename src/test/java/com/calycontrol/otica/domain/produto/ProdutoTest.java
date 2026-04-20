package com.calycontrol.otica.domain.produto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoTest {

    @Test
    @DisplayName("estoqueBaixo deve retornar true quando estoque atual <= mínimo")
    void estoqueBaixo_quandoAbaixoDoMinimo() {
        Produto p = new Produto();
        p.setEstoqueAtual(1);
        p.setEstoqueMinimo(5);
        assertThat(p.estoqueBaixo()).isTrue();
    }

    @Test
    @DisplayName("estoqueBaixo deve retornar true quando estoque igual ao mínimo")
    void estoqueBaixo_quandoIgualAoMinimo() {
        Produto p = new Produto();
        p.setEstoqueAtual(5);
        p.setEstoqueMinimo(5);
        assertThat(p.estoqueBaixo()).isTrue();
    }

    @Test
    @DisplayName("estoqueBaixo deve retornar false quando estoque acima do mínimo")
    void estoqueBaixo_quandoAcimaDoMinimo() {
        Produto p = new Produto();
        p.setEstoqueAtual(10);
        p.setEstoqueMinimo(5);
        assertThat(p.estoqueBaixo()).isFalse();
    }

    @Test
    @DisplayName("estoqueBaixo deve retornar true quando estoque é zero e mínimo é zero")
    void estoqueBaixo_quandoAmbosZero() {
        Produto p = new Produto();
        p.setEstoqueAtual(0);
        p.setEstoqueMinimo(0);
        assertThat(p.estoqueBaixo()).isTrue();
    }
}
