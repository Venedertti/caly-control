package com.calycontrol.otica.domain.usuario;

import com.calycontrol.otica.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Política de senha:
     *   - Mínimo 8 caracteres
     *   - Pelo menos 1 letra maiúscula
     *   - Pelo menos 1 letra minúscula
     *   - Pelo menos 1 dígito
     *   - Pelo menos 1 caractere especial (@#$%^&+=!*_-)
     */
    private static final Pattern SENHA_FORTE = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!*_\\-]).{8,}$"
    );

    public List<Usuario> findAll() {
        return repository.findByAtivoTrue();
    }

    public Usuario findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado: " + id));
    }

    @Transactional
    public Usuario save(Usuario usuario) {
        if (repository.findByEmail(usuario.getEmail())
                .filter(u -> !u.getId().equals(usuario.getId()))
                .isPresent()) {
            throw new BusinessException("Já existe um usuário com o e-mail informado.");
        }

        // Valida força da senha apenas quando for uma senha nova (não é hash BCrypt)
        String senha = usuario.getSenhaHash();
        if (!senha.startsWith("$2")) {
            if (!SENHA_FORTE.matcher(senha).matches()) {
                throw new BusinessException(
                    "Senha fraca. Use ao menos 8 caracteres com maiúscula, minúscula, número e símbolo (@#$%^&+=!*_-)."
                );
            }
            usuario.setSenhaHash(passwordEncoder.encode(senha));
        }

        return repository.save(usuario);
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = findById(id);
        usuario.setAtivo(false);
        repository.save(usuario);
    }
}
