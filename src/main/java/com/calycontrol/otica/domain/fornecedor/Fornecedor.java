package com.calycontrol.otica.domain.fornecedor;

import com.calycontrol.otica.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fornecedor")
@Getter
@Setter
public class Fornecedor extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "razao_social", nullable = false, length = 150)
    private String razaoSocial;

    @Column(unique = true, length = 18)
    private String cnpj;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(length = 100)
    private String representante;

    @Column(nullable = false)
    private boolean ativo = true;
}
