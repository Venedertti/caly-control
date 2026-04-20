package com.calycontrol.otica.domain.fornecedor;

import com.calycontrol.otica.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FornecedorServiceTest {

    @Mock
    private FornecedorRepository repository;

    @InjectMocks
    private FornecedorService service;

    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        fornecedor = new Fornecedor();
        fornecedor.setId(1L);
        fornecedor.setRazaoSocial("Ótica Distribuidora Ltda");
        fornecedor.setCnpj("12.345.678/0001-90");
        fornecedor.setAtivo(true);
    }

    @Test
    @DisplayName("findAll deve retornar fornecedores ativos")
    void findAll_deveRetornarFornecedoresAtivos() {
        when(repository.findByAtivoTrueOrderByRazaoSocial()).thenReturn(List.of(fornecedor));
        assertThat(service.findAll()).hasSize(1);
        verify(repository).findByAtivoTrueOrderByRazaoSocial();
    }

    @Test
    @DisplayName("findById deve retornar fornecedor quando encontrado")
    void findById_deveRetornar_quandoEncontrado() {
        when(repository.findById(1L)).thenReturn(Optional.of(fornecedor));
        assertThat(service.findById(1L)).isEqualTo(fornecedor);
    }

    @Test
    @DisplayName("findById deve lançar BusinessException quando não encontrado")
    void findById_deveLancar_quandoNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("save deve persistir quando CNPJ não está duplicado")
    void save_devePersistir_quandoCnpjNaoDuplicado() {
        when(repository.findByCnpj(fornecedor.getCnpj())).thenReturn(Optional.empty());
        when(repository.save(fornecedor)).thenReturn(fornecedor);
        assertThat(service.save(fornecedor)).isEqualTo(fornecedor);
    }

    @Test
    @DisplayName("save deve lançar BusinessException quando CNPJ pertence a outro fornecedor")
    void save_deveLancar_quandoCnpjDuplicado() {
        Fornecedor outro = new Fornecedor();
        outro.setId(2L);
        outro.setCnpj(fornecedor.getCnpj());
        when(repository.findByCnpj(fornecedor.getCnpj())).thenReturn(Optional.of(outro));
        assertThatThrownBy(() -> service.save(fornecedor))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CNPJ");
    }

    @Test
    @DisplayName("save deve aceitar fornecedor sem CNPJ")
    void save_deveAceitarSemCnpj() {
        fornecedor.setCnpj(null);
        when(repository.save(fornecedor)).thenReturn(fornecedor);
        assertThat(service.save(fornecedor)).isEqualTo(fornecedor);
        verify(repository, never()).findByCnpj(any());
    }

    @Test
    @DisplayName("save deve permitir atualizar o próprio fornecedor")
    void save_devePermitirAtualizacaoDoProprioFornecedor() {
        when(repository.findByCnpj(fornecedor.getCnpj())).thenReturn(Optional.of(fornecedor));
        when(repository.save(fornecedor)).thenReturn(fornecedor);
        assertThatNoException().isThrownBy(() -> service.save(fornecedor));
    }

    @Test
    @DisplayName("desativar deve marcar fornecedor como inativo")
    void desativar_deveMarcarcomo_inativo() {
        when(repository.findById(1L)).thenReturn(Optional.of(fornecedor));
        service.desativar(1L);
        assertThat(fornecedor.isAtivo()).isFalse();
        verify(repository).save(fornecedor);
    }

    @Test
    @DisplayName("desativar deve lançar BusinessException quando não existe")
    void desativar_deveLancar_quandoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.desativar(99L))
                .isInstanceOf(BusinessException.class);
    }
}
