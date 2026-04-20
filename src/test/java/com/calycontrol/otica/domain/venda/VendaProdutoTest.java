package com.calycontrol.otica.domain.venda;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class VendaProdutoTest {

    private VendaProduto item(BigDecimal preco, int qtd, BigDecimal desconto) {
        VendaProduto vp = new VendaProduto();
        vp.setPrecoUnitario(preco);
        vp.setQuantidade(qtd);
        vp.setDesconto(desconto);
        return vp;
    }

    @Test
    @DisplayName("subtotal deve retornar preço * qtd quando não há desconto")
    void subtotal_semDesconto() {
        VendaProduto vp = item(new BigDecimal("100.00"), 3, BigDecimal.ZERO);
        assertThat(vp.subtotal()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    @DisplayName("subtotal deve subtrair desconto do total bruto")
    void subtotal_comDesconto() {
        VendaProduto vp = item(new BigDecimal("200.00"), 2, new BigDecimal("50.00"));
        // 200 * 2 - 50 = 350
        assertThat(vp.subtotal()).isEqualByComparingTo(new BigDecimal("350.00"));
    }

    @Test
    @DisplayName("subtotal deve retornar zero para preço zero")
    void subtotal_precoZero() {
        VendaProduto vp = item(BigDecimal.ZERO, 5, BigDecimal.ZERO);
        assertThat(vp.subtotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("subtotal deve funcionar com item unitário")
    void subtotal_itemUnitario() {
        VendaProduto vp = item(new BigDecimal("49.99"), 1, BigDecimal.ZERO);
        assertThat(vp.subtotal()).isEqualByComparingTo(new BigDecimal("49.99"));
    }
}
