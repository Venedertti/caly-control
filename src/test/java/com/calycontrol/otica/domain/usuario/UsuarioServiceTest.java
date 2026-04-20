package com.calycontrol.otica.domain.usuario;

import com.calycontrol.otica.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Admin");
        usuario.setEmail("admin@calycontrol.com");
        usuario.setSenhaHash("Senha@123");
        usuario.setPerfil(PerfilUsuario.ADMIN);
        usuario.setAtivo(true);
    }

    @Test
    @DisplayName("save deve lançar BusinessException quando e-mail já pertence a outro usuário")
    void save_deveLancar_quandoEmailDuplicado() {
        Usuario outro = new Usuario();
        outro.setId(2L);
        outro.setEmail("admin@calycontrol.com");

        when(repository.findByEmail("admin@calycontrol.com")).thenReturn(Optional.of(outro));

        assertThatThrownBy(() -> service.save(usuario))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("e-mail");
    }

    @Test
    @DisplayName("save deve lançar BusinessException quando senha não atende à política")
    void save_deveLancar_quandoSenhaFraca() {
        when(repository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        usuario.setSenhaHash("fraca");

        assertThatThrownBy(() -> service.save(usuario))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Senha");
    }

    @Test
    @DisplayName("save deve aceitar senha sem maiúscula")
    void save_deveLancar_quandoSenhaSemMaiuscula() {
        when(repository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        usuario.setSenhaHash("senha@123");

        assertThatThrownBy(() -> service.save(usuario))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("save deve aceitar senha sem símbolo especial")
    void save_deveLancar_quandoSenhaSemSimbolo() {
        when(repository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        usuario.setSenhaHash("SenhaForte123");

        assertThatThrownBy(() -> service.save(usuario))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("save deve codificar a senha quando ela é texto puro válido")
    void save_deveCodificarSenha_quandoTextoPuroValido() {
        when(repository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Senha@123")).thenReturn("$2a$12$hash");
        when(repository.save(usuario)).thenReturn(usuario);

        service.save(usuario);

        assertThat(usuario.getSenhaHash()).isEqualTo("$2a$12$hash");
        verify(passwordEncoder).encode("Senha@123");
    }

    @Test
    @DisplayName("save não deve re-codificar senha que já é hash BCrypt")
    void save_naoDeveCodificar_quandoSenhaJaEhBCrypt() {
        usuario.setSenhaHash("$2a$12$hashbcryptqualquer");
        when(repository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        when(repository.save(usuario)).thenReturn(usuario);

        service.save(usuario);

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("save deve permitir atualização do próprio usuário sem conflito de e-mail")
    void save_devePermitirAtualizacaoDoProprioUsuario() {
        when(repository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("Senha@123")).thenReturn("$2a$12$hash");
        when(repository.save(usuario)).thenReturn(usuario);

        assertThatNoException().isThrownBy(() -> service.save(usuario));
    }

    @Test
    @DisplayName("desativar deve marcar usuário como inativo")
    void desativar_deveMarcarcomo_inativo() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        service.desativar(1L);

        assertThat(usuario.isAtivo()).isFalse();
        verify(repository).save(usuario);
    }

    @Test
    @DisplayName("desativar deve lançar BusinessException quando usuário não existe")
    void desativar_deveLancar_quandoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.desativar(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("99");
    }
}
