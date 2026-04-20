package com.calycontrol.otica.domain.medico;

import com.calycontrol.otica.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "medico")
@Getter
@Setter
public class Medico extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank
    @Column(nullable = false, unique = true, length = 20)
    private String crm;

    @Column(length = 20)
    private String telefone;

    @Column(nullable = false)
    private boolean ativo = true;
}
