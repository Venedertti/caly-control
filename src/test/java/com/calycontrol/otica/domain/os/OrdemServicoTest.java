package com.calycontrol.otica.domain.os;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrdemServicoTest {

    private OrdemServico os;

    @BeforeEach
    void setUp() {
        os = new OrdemServico();
        os.setItens(new ArrayList<>());
    }

    private OsProduto item(BigDecimal preco, int qtd, BigDecimal desconto) {
        OsProduto p = new OsProduto();
        p.setPrecoUnitario(preco);
        p.setQuantidade(qtd);
        p.setDesconto(desconto);
        return p;
    }

    @Test
    @DisplayName("recalcularTotal deve somar subtotais dos itens")
    void recalcularTotal_deveSomarSubtotais() {
        os.setItens(new ArrayList<>(List.of(
                item(new BigDecimal("100.00"), 2, BigDecimal.ZERO),
                item(new BigDecimal("50.00"), 1, new BigDecimal("10.00"))
        )));
        os.recalcularTotal();
        assertThat(os.getValorTotal()).isEqualByComparingTo(new BigDecimal("240.00"));
    }

    @Test
    @DisplayName("recalcularTotal com lista vazia deve resultar em zero")
    void recalcularTotal_semItens_deveSerZero() {
        os.recalcularTotal();
        assertThat(os.getValorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("podeAvancar deve retornar true para ABERTA, EM_PRODUCAO, PRONTA")
    void podeAvancar_statusPermitidos() {
        for (StatusOS s : List.of(StatusOS.ABERTA, StatusOS.EM_PRODUCAO, StatusOS.PRONTA)) {
            os.setStatus(s);
            assertThat(os.podeAvancar()).as("Status: " + s).isTrue();
        }
    }

    @Test
    @DisplayName("podeAvancar deve retornar false para ENTREGUE e CANCELADA")
    void podeAvancar_statusNaoPermitidos() {
        for (StatusOS s : List.of(StatusOS.ENTREGUE, StatusOS.CANCELADA)) {
            os.setStatus(s);
            assertThat(os.podeAvancar()).as("Status: " + s).isFalse();
        }
    }

    @Test
    @DisplayName("podeCancelar deve retornar true para ABERTA e EM_PRODUCAO")
    void podeCancelar_statusPermitidos() {
        for (StatusOS s : List.of(StatusOS.ABERTA, StatusOS.EM_PRODUCAO)) {
            os.setStatus(s);
            assertThat(os.podeCancelar()).as("Status: " + s).isTrue();
        }
    }

    @Test
    @DisplayName("podeCancelar deve retornar false para PRONTA, ENTREGUE, CANCELADA")
    void podeCancelar_statusNaoPermitidos() {
        for (StatusOS s : List.of(StatusOS.PRONTA, StatusOS.ENTREGUE, StatusOS.CANCELADA)) {
            os.setStatus(s);
            assertThat(os.podeCancelar()).as("Status: " + s).isFalse();
        }
    }

    @Test
    @DisplayName("avancar de ABERTA deve ir para EM_PRODUCAO")
    void avancar_deAbertaParaEmProducao() {
        os.setStatus(StatusOS.ABERTA);
        os.avancar();
        assertThat(os.getStatus()).isEqualTo(StatusOS.EM_PRODUCAO);
    }

    @Test
    @DisplayName("avancar de EM_PRODUCAO deve ir para PRONTA")
    void avancar_deEmProducaoParaPronta() {
        os.setStatus(StatusOS.EM_PRODUCAO);
        os.avancar();
        assertThat(os.getStatus()).isEqualTo(StatusOS.PRONTA);
    }

    @Test
    @DisplayName("avancar de PRONTA deve ir para ENTREGUE e registrar data de entrega")
    void avancar_deProntaParaEntregue() {
        os.setStatus(StatusOS.PRONTA);
        os.avancar();
        assertThat(os.getStatus()).isEqualTo(StatusOS.ENTREGUE);
        assertThat(os.getDataEntrega()).isNotNull();
    }

    @Test
    @DisplayName("avancar de ENTREGUE não deve mudar status")
    void avancar_deEntregue_naoAltera() {
        os.setStatus(StatusOS.ENTREGUE);
        os.avancar();
        assertThat(os.getStatus()).isEqualTo(StatusOS.ENTREGUE);
    }
}
