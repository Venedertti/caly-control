package com.calycontrol.otica.domain.usuario;

import com.calycontrol.otica.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nome;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank
    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilUsuario perfil;

    @Column(nullable = false)
    private boolean ativo = true;

    /** Contador de falhas consecutivas. Resetado ao fazer login com sucesso. */
    @Column(name = "tentativas_falha", nullable = false)
    private int tentativasFalha = 0;

    /** Conta bloqueada até este instante. NULL = desbloqueada. */
    @Column(name = "bloqueado_ate")
    private java.time.LocalDateTime bloqueadoAte;

    /** Retorna true se a conta está atualmente bloqueada por falhas excessivas. */
    public boolean isContaBloqueada() {
        return bloqueadoAte != null && bloqueadoAte.isAfter(java.time.LocalDateTime.now());
    }
}
