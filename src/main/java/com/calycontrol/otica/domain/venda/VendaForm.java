package com.calycontrol.otica.domain.venda;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class VendaForm {

    @NotNull(message = "Selecione um cliente.")
    private Long clienteId;

    @NotNull(message = "Selecione a forma de pagamento.")
    private FormaPagamento formaPagamento;

    private int parcelas = 1;

    @Size(min = 1, message = "Adicione pelo menos um produto.")
    private List<ItemVendaForm> itens = new ArrayList<>();

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }

    public int getParcelas() { return parcelas; }
    public void setParcelas(int parcelas) { this.parcelas = parcelas; }

    public List<ItemVendaForm> getItens() { return itens; }
    public void setItens(List<ItemVendaForm> itens) { this.itens = itens; }
}
