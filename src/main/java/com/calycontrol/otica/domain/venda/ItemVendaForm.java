package com.calycontrol.otica.domain.venda;

import java.math.BigDecimal;

public class ItemVendaForm {
    private Long produtoId;
    private int quantidade = 1;
    private BigDecimal precoUnitario = BigDecimal.ZERO;
    private BigDecimal desconto = BigDecimal.ZERO;

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

    public BigDecimal getDesconto() { return desconto; }
    public void setDesconto(BigDecimal desconto) { this.desconto = desconto; }
}
