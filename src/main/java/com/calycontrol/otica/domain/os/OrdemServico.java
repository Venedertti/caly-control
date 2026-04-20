package com.calycontrol.otica.domain.os;

import com.calycontrol.otica.domain.cliente.Cliente;
import com.calycontrol.otica.domain.receita.ReceitaOptica;
import com.calycontrol.otica.domain.usuario.Usuario;
import com.calycontrol.otica.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordem_servico")
@Getter
@Setter
public class OrdemServico extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receita_id")
    private ReceitaOptica receita;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "numero_os", nullable = false, unique = true, length = 20)
    private String numeroOs;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusOS status = StatusOS.ABERTA;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "valor_pago", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Column(name = "data_abertura", nullable = false)
    private LocalDate dataAbertura = LocalDate.now();

    @Column(name = "data_previsao")
    private LocalDate dataPrevisao;

    @Column(name = "data_entrega")
    private LocalDate dataEntrega;

    @Column(length = 500)
    private String observacoes;

    @OneToMany(mappedBy = "os", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OsProduto> itens = new ArrayList<>();

    public void recalcularTotal() {
        this.valorTotal = itens.stream()
                .map(OsProduto::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Avança para o próximo status no fluxo normal. */
    public void avancar() {
        this.status = switch (status) {
            case ABERTA      -> StatusOS.EM_PRODUCAO;
            case EM_PRODUCAO -> StatusOS.PRONTA;
            case PRONTA      -> StatusOS.ENTREGUE;
            default          -> this.status; // ENTREGUE e CANCELADA não avançam
        };
        if (this.status == StatusOS.ENTREGUE) {
            this.dataEntrega = LocalDate.now();
        }
    }

    public boolean podeAvancar() {
        return status == StatusOS.ABERTA
            || status == StatusOS.EM_PRODUCAO
            || status == StatusOS.PRONTA;
    }

    public boolean podeCancelar() {
        return status == StatusOS.ABERTA || status == StatusOS.EM_PRODUCAO;
    }
}
