package com.calycontrol.otica.domain.venda;

import com.calycontrol.otica.domain.cliente.Cliente;
import com.calycontrol.otica.domain.usuario.Usuario;
import com.calycontrol.otica.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venda")
@Getter
@Setter
public class Venda extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "numero_venda", nullable = false, unique = true, length = 20)
    private String numeroVenda;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 20)
    private FormaPagamento formaPagamento;

    @Column(nullable = false)
    private int parcelas = 1;

    @Column(name = "data_venda", nullable = false)
    private LocalDateTime dataVenda = LocalDateTime.now();

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<VendaProduto> itens = new ArrayList<>();

    /** Recalcula valor_total a partir dos itens. Chamar antes de salvar. */
    public void recalcularTotal() {
        this.valorTotal = itens.stream()
                .map(VendaProduto::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
