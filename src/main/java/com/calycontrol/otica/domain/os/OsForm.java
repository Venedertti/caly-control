package com.calycontrol.otica.domain.os;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OsForm {

    private Long clienteId;
    private Long receitaId;
    private LocalDate dataPrevisao;
    private BigDecimal valorPago = BigDecimal.ZERO;
    private String observacoes;
    private List<ItemOsForm> itens = new ArrayList<>();

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Long getReceitaId() { return receitaId; }
    public void setReceitaId(Long receitaId) { this.receitaId = receitaId; }

    public LocalDate getDataPrevisao() { return dataPrevisao; }
    public void setDataPrevisao(LocalDate dataPrevisao) { this.dataPrevisao = dataPrevisao; }

    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public List<ItemOsForm> getItens() { return itens; }
    public void setItens(List<ItemOsForm> itens) { this.itens = itens; }
}
