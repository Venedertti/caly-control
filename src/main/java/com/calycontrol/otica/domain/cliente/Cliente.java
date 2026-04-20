package com.calycontrol.otica.domain.cliente;

import com.calycontrol.otica.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
@Getter
@Setter
public class Cliente extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nome;

    @Column(unique = true, length = 14)
    private String cpf;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(length = 9)
    private String cep;

    @Column(length = 255)
    private String endereco;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private boolean ativo = true;

    /** LGPD Art. 18, IV — registra que os dados pessoais foram apagados/pseudonimizados. */
    @Column(nullable = false)
    private boolean anonimizado = false;

    @Column(name = "data_anonimizacao")
    private LocalDateTime dataAnonimizacao;
}
