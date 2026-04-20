package com.calycontrol.otica.config;

import com.calycontrol.otica.domain.audit.AuditLogin;
import com.calycontrol.otica.domain.audit.AuditLoginRepository;
import com.calycontrol.otica.domain.usuario.Usuario;
import com.calycontrol.otica.domain.usuario.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proteção em duas camadas independentes:
 *
 *  Camada 1 — IP (em memória):
 *    5 falhas em 5 min → IP bloqueado 15 min.
 *    Rápido, sem I/O. Impede bots simples e scanners.
 *
 *  Camada 2 — Conta (persistente no banco):
 *    10 falhas consecutivas → conta bloqueada 2h.
 *    Persiste restarts. Impede ataques distribuídos (botnet/IPs rotativos).
 *
 *  Audit log (persistente):
 *    Toda tentativa gravada em audit_login com IP, user-agent e motivo.
 */
@Component
@RequiredArgsConstructor
public class BruteForceProtectionListener {

    // ── Limites IP ────────────────────────────────────────────────────────────
    private static final int  IP_MAX_FAILURES   = 5;
    private static final long IP_WINDOW_SECONDS = 300;   // 5 min
    private static final long IP_BLOCK_SECONDS  = 900;   // 15 min

    // ── Limites conta ─────────────────────────────────────────────────────────
    private static final int  ACCOUNT_MAX_FAILURES = 10;
    private static final long ACCOUNT_BLOCK_HOURS  = 2;

    private final Map<String, long[]> ipAttempts = new ConcurrentHashMap<>();

    private final UsuarioRepository    usuarioRepository;
    private final AuditLoginRepository auditRepository;

    // ── Evento: falha de autenticação ─────────────────────────────────────────

    @EventListener
    @Transactional
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        String email     = extractEmail(event);
        String ip        = resolveIp();
        String userAgent = resolveUserAgent();
        String motivo    = event.getException().getClass().getSimpleName();

        // 1. Bloqueio por IP (em memória)
        if (ip != null) registrarFalhaIp(ip);

        // 2. Bloqueio por conta + audit log (banco)
        Optional<Usuario> opt = (email != null)
                ? usuarioRepository.findByEmail(email)
                : Optional.empty();

        if (opt.isPresent()) {
            Usuario u = opt.get();

            // Só incrementa falhas para erro de credenciais — não para conta já bloqueada
            if (motivo.contains("BadCredentials") || motivo.contains("Credentials")) {
                u.setTentativasFalha(u.getTentativasFalha() + 1);
                if (u.getTentativasFalha() >= ACCOUNT_MAX_FAILURES) {
                    u.setBloqueadoAte(LocalDateTime.now().plusHours(ACCOUNT_BLOCK_HOURS));
                }
                usuarioRepository.save(u);
            }

            auditRepository.save(AuditLogin.falha(email, u.getId(), ip, userAgent, motivo));
        } else if (email != null) {
            // E-mail não existe no sistema — grava mesmo assim (detecta enumeração de usuários)
            auditRepository.save(AuditLogin.falha(email, null, ip, userAgent, "USER_NOT_FOUND"));
        }
    }

    // ── Evento: login bem-sucedido ────────────────────────────────────────────

    @EventListener
    @Transactional
    public void onSuccess(AuthenticationSuccessEvent event) {
        String ip        = resolveIp();
        String userAgent = resolveUserAgent();
        String email     = event.getAuthentication().getName();

        // Reset bloqueio de IP
        if (ip != null) ipAttempts.remove(ip);

        // Reset bloqueio de conta + audit log
        usuarioRepository.findByEmail(email).ifPresent(u -> {
            u.setTentativasFalha(0);
            u.setBloqueadoAte(null);
            usuarioRepository.save(u);
            auditRepository.save(AuditLogin.sucesso(email, u.getId(), ip, userAgent));
        });
    }

    // ── Consultas públicas (usadas pelo LoginController e SecurityConfig) ──────

    public boolean isIpBlocked(String ip) {
        long[] entry = ipAttempts.get(ip);
        if (entry == null || entry[0] < IP_MAX_FAILURES) return false;
        long elapsed = Instant.now().getEpochSecond() - entry[1];
        if (elapsed > IP_BLOCK_SECONDS) { ipAttempts.remove(ip); return false; }
        return true;
    }

    public long ipSecondsRemaining(String ip) {
        long[] entry = ipAttempts.get(ip);
        if (entry == null) return 0;
        return Math.max(0, IP_BLOCK_SECONDS - (Instant.now().getEpochSecond() - entry[1]));
    }

    // ── Internos ──────────────────────────────────────────────────────────────

    private void registrarFalhaIp(String ip) {
        long now = Instant.now().getEpochSecond();
        ipAttempts.compute(ip, (k, v) -> {
            if (v == null || (now - v[1]) > IP_WINDOW_SECONDS) return new long[]{1, now};
            v[0]++;
            if (v[0] >= IP_MAX_FAILURES) v[1] = now; // reinicia janela de bloqueio
            return v;
        });
    }

    private String extractEmail(AbstractAuthenticationFailureEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        return (principal instanceof String s) ? s : null;
    }

    public static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    private String resolveIp() {
        try {
            HttpServletRequest req =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            return resolveClientIp(req);
        } catch (Exception e) { return null; }
    }

    private String resolveUserAgent() {
        try {
            HttpServletRequest req =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String ua = req.getHeader("User-Agent");
            return (ua != null && ua.length() > 500) ? ua.substring(0, 500) : ua;
        } catch (Exception e) { return null; }
    }
}
