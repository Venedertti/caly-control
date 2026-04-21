package com.calycontrol.otica.config;

import com.calycontrol.otica.domain.audit.AuditLoginRepository;
import com.calycontrol.otica.domain.usuario.Usuario;
import com.calycontrol.otica.domain.usuario.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BruteForceProtectionListenerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuditLoginRepository auditRepository;

    @InjectMocks
    private BruteForceProtectionListener listener;

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    private void setupRequest(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(ip);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private AbstractAuthenticationFailureEvent mockFailureEvent(String email) {
        AbstractAuthenticationFailureEvent event = mock(AbstractAuthenticationFailureEvent.class);
        Authentication auth = mock(Authentication.class);
        when(event.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(email);
        when(event.getException()).thenReturn(new BadCredentialsException("bad credentials"));
        return event;
    }

    private AuthenticationSuccessEvent mockSuccessEvent(String email) {
        AuthenticationSuccessEvent event = mock(AuthenticationSuccessEvent.class);
        Authentication auth = mock(Authentication.class);
        when(event.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn(email);
        return event;
    }

    // ── resolveClientIp ──────────────────────────────────────────────────────

    @Test
    @DisplayName("resolveClientIp deve retornar RemoteAddr quando não há X-Forwarded-For")
    void resolveClientIp_semHeader_retornaRemoteAddr() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("192.168.1.1");
        assertThat(BruteForceProtectionListener.resolveClientIp(req)).isEqualTo("192.168.1.1");
    }

    @Test
    @DisplayName("resolveClientIp deve retornar o primeiro IP do header X-Forwarded-For")
    void resolveClientIp_comXForwardedFor_retornaPrimeiro() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2, 10.0.0.3");
        assertThat(BruteForceProtectionListener.resolveClientIp(req)).isEqualTo("10.0.0.1");
    }

    // ── isIpBlocked ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("isIpBlocked deve retornar false para IP desconhecido")
    void isIpBlocked_ipDesconhecido_retornaFalso() {
        assertThat(listener.isIpBlocked("1.2.3.4")).isFalse();
    }

    @Test
    @DisplayName("isIpBlocked deve retornar false com menos de 5 falhas")
    void isIpBlocked_poucasFalhas_retornaFalso() {
        setupRequest("5.5.5.5");
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(auditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        AbstractAuthenticationFailureEvent event = mockFailureEvent("test@test.com");

        for (int i = 0; i < 4; i++) {
            listener.onFailure(event);
        }

        assertThat(listener.isIpBlocked("5.5.5.5")).isFalse();
    }

    @Test
    @DisplayName("isIpBlocked deve retornar true após atingir 5 falhas")
    void isIpBlocked_aposLimite_retornaVerdadeiro() {
        setupRequest("6.6.6.6");
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(auditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        AbstractAuthenticationFailureEvent event = mockFailureEvent("test@test.com");

        for (int i = 0; i < 5; i++) {
            listener.onFailure(event);
        }

        assertThat(listener.isIpBlocked("6.6.6.6")).isTrue();
    }

    // ── ipSecondsRemaining ───────────────────────────────────────────────────

    @Test
    @DisplayName("ipSecondsRemaining deve retornar 0 para IP desconhecido")
    void ipSecondsRemaining_ipDesconhecido_retornaZero() {
        assertThat(listener.ipSecondsRemaining("9.9.9.9")).isEqualTo(0);
    }

    @Test
    @DisplayName("ipSecondsRemaining deve retornar valor positivo após bloqueio de IP")
    void ipSecondsRemaining_aposBlock_retornaPositivo() {
        setupRequest("7.7.7.7");
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(auditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        AbstractAuthenticationFailureEvent event = mockFailureEvent("user@test.com");

        for (int i = 0; i < 5; i++) {
            listener.onFailure(event);
        }

        assertThat(listener.ipSecondsRemaining("7.7.7.7")).isGreaterThan(0);
    }

    // ── onFailure — sem usuário no banco ────────────────────────────────────

    @Test
    @DisplayName("onFailure deve gravar audit USER_NOT_FOUND quando e-mail não existe no banco")
    void onFailure_emailNaoEncontrado_gravaAuditUserNotFound() {
        setupRequest("2.2.2.2");
        when(usuarioRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());
        when(auditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onFailure(mockFailureEvent("notfound@test.com"));

        verify(auditRepository).save(argThat(a -> !a.isSucesso()));
    }

    @Test
    @DisplayName("onFailure com principal não-String não deve interagir com repositórios")
    void onFailure_semPrincipalString_naoInterageComRepositorios() {
        AbstractAuthenticationFailureEvent event = mock(AbstractAuthenticationFailureEvent.class);
        Authentication auth = mock(Authentication.class);
        when(event.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(new Object());
        when(event.getException()).thenReturn(new BadCredentialsException("bad"));

        listener.onFailure(event);

        verifyNoInteractions(auditRepository);
        verifyNoInteractions(usuarioRepository);
    }

    // ── onFailure — usuário encontrado ──────────────────────────────────────

    @Test
    @DisplayName("onFailure deve incrementar tentativas_falha em BadCredentials")
    void onFailure_badCredentials_incrementaTentativas() {
        setupRequest("3.3.3.3");
        Usuario u = new Usuario();
        u.setId(1L);
        u.setTentativasFalha(2);

        when(usuarioRepository.findByEmail("user@caly.com")).thenReturn(Optional.of(u));
        when(usuarioRepository.save(u)).thenReturn(u);
        when(auditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onFailure(mockFailureEvent("user@caly.com"));

        assertThat(u.getTentativasFalha()).isEqualTo(3);
    }

    @Test
    @DisplayName("onFailure deve bloquear conta quando tentativas atingem o limite")
    void onFailure_atingeLimite_bloqueaConta() {
        setupRequest("4.4.4.4");
        Usuario u = new Usuario();
        u.setId(1L);
        u.setTentativasFalha(9);

        when(usuarioRepository.findByEmail("lock@caly.com")).thenReturn(Optional.of(u));
        when(usuarioRepository.save(u)).thenReturn(u);
        when(auditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onFailure(mockFailureEvent("lock@caly.com"));

        assertThat(u.getTentativasFalha()).isEqualTo(10);
        assertThat(u.getBloqueadoAte()).isNotNull();
    }

    @Test
    @DisplayName("onFailure sem RequestContext não deve lançar exceção e deve processar normalmente")
    void onFailure_semRequestContext_processaComIpNulo() {
        // sem RequestContextHolder configurado → ip = null → registrarFalhaIp não chamado
        when(usuarioRepository.findByEmail("email@test.com")).thenReturn(Optional.empty());
        when(auditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onFailure(mockFailureEvent("email@test.com"));

        verify(auditRepository).save(any());
    }

    // ── onSuccess ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("onSuccess deve resetar tentativas e bloqueio do usuário")
    void onSuccess_deveResetarBloqueioUsuario() {
        setupRequest("8.8.8.8");
        Usuario u = new Usuario();
        u.setId(1L);
        u.setTentativasFalha(5);
        u.setBloqueadoAte(LocalDateTime.now().plusHours(1));

        when(usuarioRepository.findByEmail("admin@caly.com")).thenReturn(Optional.of(u));
        when(usuarioRepository.save(u)).thenReturn(u);
        when(auditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onSuccess(mockSuccessEvent("admin@caly.com"));

        assertThat(u.getTentativasFalha()).isEqualTo(0);
        assertThat(u.getBloqueadoAte()).isNull();
    }

    @Test
    @DisplayName("onSuccess deve gravar audit de sucesso")
    void onSuccess_deveGravarAuditSucesso() {
        setupRequest("8.8.8.8");
        Usuario u = new Usuario();
        u.setId(1L);
        u.setTentativasFalha(0);

        when(usuarioRepository.findByEmail("ok@caly.com")).thenReturn(Optional.of(u));
        when(usuarioRepository.save(u)).thenReturn(u);
        when(auditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onSuccess(mockSuccessEvent("ok@caly.com"));

        verify(auditRepository).save(argThat(a -> a.isSucesso()));
    }

    @Test
    @DisplayName("onSuccess deve remover IP do mapa de bloqueios")
    void onSuccess_deveRemoverIpDoBloqueio() {
        setupRequest("1.1.1.1");
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(auditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AbstractAuthenticationFailureEvent failEvent = mockFailureEvent("x@x.com");
        for (int i = 0; i < 5; i++) {
            listener.onFailure(failEvent);
        }
        assertThat(listener.isIpBlocked("1.1.1.1")).isTrue();

        listener.onSuccess(mockSuccessEvent("user@x.com"));

        assertThat(listener.isIpBlocked("1.1.1.1")).isFalse();
    }

    @Test
    @DisplayName("onSuccess sem RequestContext não deve lançar exceção")
    void onSuccess_semRequestContext_naoLancaExcecao() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setTentativasFalha(0);

        when(usuarioRepository.findByEmail("noip@caly.com")).thenReturn(Optional.of(u));
        when(usuarioRepository.save(u)).thenReturn(u);
        when(auditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onSuccess(mockSuccessEvent("noip@caly.com"));

        verify(auditRepository).save(any());
    }
}
