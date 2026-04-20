package com.calycontrol.otica.domain.os;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OsProdutoTest {

    private OsProduto item(BigDecimal preco, int qtd, BigDecimal desconto) {
        OsProduto p = new OsProduto();
        p.setPrecoUnitario(preco);
        p.setQuantidade(qtd);
        p.setDesconto(desconto);
        return p;
    }

    @Test
    @DisplayName("subtotal sem desconto deve ser preco * quantidade")
    void subtotal_semDesconto() {
        assertThat(item(new BigDecimal("80.00"), 3, BigDecimal.ZERO).subtotal())
                .isEqualByComparingTo(new BigDecimal("240.00"));
    }

    @Test
    @DisplayName("subtotal com desconto deve subtrair do total bruto")
    void subtotal_comDesconto() {
        // 100 * 2 - 30 = 170
        assertThat(item(new BigDecimal("100.00"), 2, new BigDecimal("30.00")).subtotal())
                .isEqualByComparingTo(new BigDecimal("170.00"));
    }

    @Test
    @DisplayName("subtotal unitário sem desconto deve retornar o preço")
    void subtotal_unitario() {
        assertThat(item(new BigDecimal("299.90"), 1, BigDecimal.ZERO).subtotal())
                .isEqualByComparingTo(new BigDecimal("299.90"));
    }
}
