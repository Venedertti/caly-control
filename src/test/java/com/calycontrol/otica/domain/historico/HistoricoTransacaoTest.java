package com.calycontrol.otica.domain.historico;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class HistoricoTransacaoTest {

    @Test
    @DisplayName("deve aceitar e retornar todos os campos via setters e getters")
    void camposLombok_devemSerAcessiveis() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        HistoricoTransacao h = new HistoricoTransacao();
        h.setId(1L);
        h.setTipo("ORDEM_SERVICO");
        h.setSubtipo("ENTREGUE");
        h.setOrigemTabela("ordem_servico");
        h.setOrigemId(42L);
        h.setNumeroReferencia("OS-20260420");
        h.setDataTransacao(now);
        h.setDataRegistradoEm(now);
        h.setClienteId(5L);
        h.setClienteNome("Maria Souza");
        h.setClienteCpf("987.654.321-00");
        h.setClienteTelefone("11988888888");
        h.setClienteEmail("maria@test.com");
        h.setUsuarioId(3L);
        h.setUsuarioNome("Técnico");
        h.setUsuarioPerfil("TECNICO");
        h.setValorBruto(new BigDecimal("500.00"));
        h.setValorDescontoTotal(new BigDecimal("50.00"));
        h.setValorTotal(new BigDecimal("450.00"));
        h.setValorPago(new BigDecimal("450.00"));
        h.setFormaPagamento("CREDITO");
        h.setParcelas(3);
        h.setItensResumo("Lente x1 @ R$ 450,00");
        h.setItensQuantidadeTotal(1);
        h.setItensCount(1);
        h.setReceitaId(10L);
        h.setMedicoNome("Dr. Silva");
        h.setMedicoCrm("98765-SP");
        h.setReceitaDataEmissao(today.minusDays(30));
        h.setReceitaValidade(today.plusYears(1));
        h.setOsDataPrevisao(today.plusDays(7));
        h.setOsDataEntrega(today.plusDays(9));
        h.setOsStatus("ENTREGUE");
        h.setProdutoId(7L);
        h.setProdutoCodigo("LNT-001");
        h.setProdutoDescricao("Lente Multifocal");
        h.setProdutoTipo("LENTE");
        h.setProdutoMarca("Zeiss");
        h.setProdutoFornecedor("Ótica Distribuidora");
        h.setMovimentacaoQuantidade(2);
        h.setObservacoes("Observação de teste");

        assertThat(h.getId()).isEqualTo(1L);
        assertThat(h.getTipo()).isEqualTo("ORDEM_SERVICO");
        assertThat(h.getSubtipo()).isEqualTo("ENTREGUE");
        assertThat(h.getOrigemTabela()).isEqualTo("ordem_servico");
        assertThat(h.getOrigemId()).isEqualTo(42L);
        assertThat(h.getNumeroReferencia()).isEqualTo("OS-20260420");
        assertThat(h.getDataTransacao()).isEqualTo(now);
        assertThat(h.getDataRegistradoEm()).isEqualTo(now);
        assertThat(h.getClienteId()).isEqualTo(5L);
        assertThat(h.getClienteNome()).isEqualTo("Maria Souza");
        assertThat(h.getClienteCpf()).isEqualTo("987.654.321-00");
        assertThat(h.getClienteTelefone()).isEqualTo("11988888888");
        assertThat(h.getClienteEmail()).isEqualTo("maria@test.com");
        assertThat(h.getUsuarioId()).isEqualTo(3L);
        assertThat(h.getUsuarioNome()).isEqualTo("Técnico");
        assertThat(h.getUsuarioPerfil()).isEqualTo("TECNICO");
        assertThat(h.getValorBruto()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(h.getValorDescontoTotal()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(h.getValorTotal()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(h.getValorPago()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(h.getFormaPagamento()).isEqualTo("CREDITO");
        assertThat(h.getParcelas()).isEqualTo(3);
        assertThat(h.getItensResumo()).isEqualTo("Lente x1 @ R$ 450,00");
        assertThat(h.getItensQuantidadeTotal()).isEqualTo(1);
        assertThat(h.getItensCount()).isEqualTo(1);
        assertThat(h.getReceitaId()).isEqualTo(10L);
        assertThat(h.getMedicoNome()).isEqualTo("Dr. Silva");
        assertThat(h.getMedicoCrm()).isEqualTo("98765-SP");
        assertThat(h.getReceitaDataEmissao()).isEqualTo(today.minusDays(30));
        assertThat(h.getReceitaValidade()).isEqualTo(today.plusYears(1));
        assertThat(h.getOsDataPrevisao()).isEqualTo(today.plusDays(7));
        assertThat(h.getOsDataEntrega()).isEqualTo(today.plusDays(9));
        assertThat(h.getOsStatus()).isEqualTo("ENTREGUE");
        assertThat(h.getProdutoId()).isEqualTo(7L);
        assertThat(h.getProdutoCodigo()).isEqualTo("LNT-001");
        assertThat(h.getProdutoDescricao()).isEqualTo("Lente Multifocal");
        assertThat(h.getProdutoTipo()).isEqualTo("LENTE");
        assertThat(h.getProdutoMarca()).isEqualTo("Zeiss");
        assertThat(h.getProdutoFornecedor()).isEqualTo("Ótica Distribuidora");
        assertThat(h.getMovimentacaoQuantidade()).isEqualTo(2);
        assertThat(h.getObservacoes()).isEqualTo("Observação de teste");
    }

    @Test
    @DisplayName("dataRegistradoEm deve ter valor padrão não nulo")
    void dataRegistradoEm_deveSerInicializadaAutomaticamente() {
        HistoricoTransacao h = new HistoricoTransacao();
        assertThat(h.getDataRegistradoEm()).isNotNull();
    }
}
