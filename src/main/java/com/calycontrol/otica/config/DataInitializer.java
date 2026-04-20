package com.calycontrol.otica.config;

import com.calycontrol.otica.domain.usuario.PerfilUsuario;
import com.calycontrol.otica.domain.usuario.Usuario;
import com.calycontrol.otica.domain.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        criarAdminSeNaoExistir();
    }

    private void criarAdminSeNaoExistir() {
        final String email = "admin@otica.com";

        if (usuarioRepository.findByEmail(email).isPresent()) {
            log.info("Usuário admin já existe, nenhuma ação necessária.");
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome("Administrador");
        admin.setEmail(email);
        admin.setSenhaHash(passwordEncoder.encode("admin123"));
        admin.setPerfil(PerfilUsuario.ADMIN);
        admin.setAtivo(true);

        usuarioRepository.save(admin);
        log.info("Usuário admin criado com sucesso: {}", email);
    }
}
