package com.calycontrol.otica.domain.audit;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_login")
public class AuditLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_tentativa", nullable = false, length = 150)
    private String emailTentativa;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false, length = 45)
    private String ip;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(nullable = false)
    private boolean sucesso = false;

    @Column(name = "motivo_falha", length = 100)
    private String motivoFalha;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    // ── factory methods ──────────────────────────────────────────────────────

    public static AuditLogin sucesso(String email, Long usuarioId, String ip, String userAgent) {
        AuditLogin a = new AuditLogin();
        a.emailTentativa = email;
        a.usuarioId      = usuarioId;
        a.ip             = ip;
        a.userAgent      = userAgent;
        a.sucesso        = true;
        return a;
    }

    public static AuditLogin falha(String email, Long usuarioId,
                                   String ip, String userAgent, String motivo) {
        AuditLogin a = new AuditLogin();
        a.emailTentativa = email;
        a.usuarioId      = usuarioId;
        a.ip             = ip;
        a.userAgent      = userAgent;
        a.sucesso        = false;
        a.motivoFalha    = motivo;
        return a;
    }

    // ── getters ──────────────────────────────────────────────────────────────

    public Long getId()                  { return id; }
    public String getEmailTentativa()    { return emailTentativa; }
    public Long getUsuarioId()           { return usuarioId; }
    public String getIp()                { return ip; }
    public String getUserAgent()         { return userAgent; }
    public boolean isSucesso()           { return sucesso; }
    public String getMotivoFalha()       { return motivoFalha; }
    public LocalDateTime getCriadoEm()   { return criadoEm; }
}
