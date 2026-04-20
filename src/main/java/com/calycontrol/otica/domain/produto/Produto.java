package com.calycontrol.otica.domain.produto;

import com.calycontrol.otica.domain.fornecedor.Fornecedor;
import com.calycontrol.otica.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "produto")
@Getter
@Setter
public class Produto extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String descricao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoProduto tipo;

    @Column(length = 80)
    private String marca;

    @Column(name = "preco_custo", precision = 10, scale = 2)
    private BigDecimal precoCusto;

    @NotNull
    @Column(name = "preco_venda", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoVenda;

    @PositiveOrZero
    @Column(name = "estoque_atual", nullable = false)
    private int estoqueAtual = 0;

    @PositiveOrZero
    @Column(name = "estoque_minimo", nullable = false)
    private int estoqueMinimo = 0;

    @Column(nullable = false)
    private boolean ativo = true;

    public boolean estoqueBaixo() {
        return estoqueAtual <= estoqueMinimo;
    }
}
