package com.calycontrol.otica.domain.receita;

import com.calycontrol.otica.domain.cliente.Cliente;
import com.calycontrol.otica.domain.medico.Medico;
import com.calycontrol.otica.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "receita_optica")
@Getter
@Setter
public class ReceitaOptica extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @NotNull
    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @NotNull
    @Column(nullable = false)
    private LocalDate validade;

    // Olho Direito
    @Column(name = "od_esf", precision = 5, scale = 2)
    private BigDecimal odEsf;
    @Column(name = "od_cil", precision = 5, scale = 2)
    private BigDecimal odCil;
    @Column(name = "od_eixo")
    private Integer odEixo;

    // Olho Esquerdo
    @Column(name = "oe_esf", precision = 5, scale = 2)
    private BigDecimal oeEsf;
    @Column(name = "oe_cil", precision = 5, scale = 2)
    private BigDecimal oeCil;
    @Column(name = "oe_eixo")
    private Integer oeEixo;

    @Column(precision = 5, scale = 2)
    private BigDecimal adicao;

    @Column(name = "dp_od", precision = 5, scale = 2)
    private BigDecimal dpOd;

    @Column(name = "dp_oe", precision = 5, scale = 2)
    private BigDecimal dpOe;

    @Column(length = 500)
    private String observacoes;

    /**
     * Hipótese autorizativa do Art. 11 da LGPD para o tratamento deste dado de saúde.
     * Valor padrão ART_11_II_C: tutela da saúde em procedimento realizado por
     * profissionais de saúde ou serviços de saúde (optometria).
     */
    @Column(name = "base_legal", nullable = false, length = 30)
    private String baseLegal = "ART_11_II_C";

    public boolean isExpirada() {
        return LocalDate.now().isAfter(validade);
    }
}
