package com.calycontrol.otica.config;

import com.calycontrol.otica.domain.usuario.Usuario;
import com.calycontrol.otica.domain.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // habilita @PreAuthorize nos controllers (camada real de autorização)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ── Autorização de URLs ──────────────────────────────────────────
            // Primeira linha de defesa por URL (defense-in-depth).
            // A segunda linha são os @PreAuthorize nos controllers.
            .authorizeHttpRequests(auth -> auth
                // Apenas os arquivos estáticos conhecidos são públicos — nunca usar /** genérico
                .requestMatchers("/css/caly.css", "/favicon.svg").permitAll()
                // Área administrativa restrita a ADMIN
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                // LGPD — atendimento a direitos do titular (Art. 18). Restrito a ADMIN.
                .requestMatchers("/lgpd/**").hasRole("ADMIN")
                // Demais áreas exigem login (qualquer perfil ativo)
                .anyRequest().authenticated()
            )

            // ── Login ────────────────────────────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?erro")
                .permitAll()
            )

            // ── Logout ───────────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?saiu")
                .permitAll()
            )

            // ── Sessão ───────────────────────────────────────────────────────
            .sessionManagement(session -> session
                .invalidSessionUrl("/login?sessaoExpirada")
                // Previne session fixation: gera novo ID de sessão após login
                .sessionFixation().newSession()
                // Apenas uma sessão simultânea por usuário
                .maximumSessions(1)
                .expiredUrl("/login?sessaoExpirada")
            )

            // ── Cabeçalhos de segurança HTTP ─────────────────────────────────
            // Protege contra clickjacking, sniffing de MIME, XSS via browser
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(ct -> {})
                .referrerPolicy(ref ->
                    ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                .contentSecurityPolicy(csp ->
                    csp.policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' cdn.jsdelivr.net; " +
                        "style-src 'self' 'unsafe-inline' cdn.jsdelivr.net; " +
                        "font-src 'self' cdn.jsdelivr.net; " +
                        "img-src 'self' data:; " +
                        "frame-ancestors 'none'"
                    ))
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            Usuario u = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

            // Passa o estado real da conta para o DaoAuthenticationProvider:
            //   enabled          → ativo no sistema
            //   accountNonLocked → não está bloqueado por falhas excessivas
            // O Spring Security lança exceções apropriadas para cada caso,
            // que o listener captura e grava no audit log com o motivo correto.
            return new org.springframework.security.core.userdetails.User(
                    u.getEmail(),
                    u.getSenhaHash(),
                    u.isAtivo(),            // enabled
                    true,                   // accountNonExpired
                    true,                   // credentialsNonExpired
                    !u.isContaBloqueada(),  // accountNonLocked
                    List.of(new SimpleGrantedAuthority("ROLE_" + u.getPerfil().name()))
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt com custo 12 — aumenta o tempo de cada tentativa de brute force
        // Custo padrão é 10; 12 ≈ 4x mais lento por hash, imperceptível para login normal
        return new BCryptPasswordEncoder(12);
    }
}
