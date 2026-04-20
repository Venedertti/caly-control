package com.calycontrol.otica.domain.venda;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VendaTest {

    private VendaProduto item(BigDecimal preco, int qtd, BigDecimal desconto) {
        VendaProduto vp = new VendaProduto();
        vp.setPrecoUnitario(preco);
        vp.setQuantidade(qtd);
        vp.setDesconto(desconto);
        return vp;
    }

    @Test
    @DisplayName("recalcularTotal deve somar subtotais dos itens")
    void recalcularTotal_deveSomarSubtotais() {
        Venda venda = new Venda();
        venda.setItens(new ArrayList<>(List.of(
                item(new BigDecimal("200.00"), 1, BigDecimal.ZERO),
                item(new BigDecimal("50.00"), 2, new BigDecimal("10.00"))
        )));
        venda.recalcularTotal();
        // 200 + (100 - 10) = 290
        assertThat(venda.getValorTotal()).isEqualByComparingTo(new BigDecimal("290.00"));
    }

    @Test
    @DisplayName("recalcularTotal com lista vazia deve resultar em zero")
    void recalcularTotal_semItens_deveSerZero() {
        Venda venda = new Venda();
        venda.setItens(new ArrayList<>());
        venda.recalcularTotal();
        assertThat(venda.getValorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
