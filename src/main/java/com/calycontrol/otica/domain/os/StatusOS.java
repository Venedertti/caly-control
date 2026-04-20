package com.calycontrol.otica.domain.os;

public enum StatusOS {
    ABERTA("Aberta"),
    EM_PRODUCAO("Em Produção"),
    PRONTA("Pronta"),
    ENTREGUE("Entregue"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusOS(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
