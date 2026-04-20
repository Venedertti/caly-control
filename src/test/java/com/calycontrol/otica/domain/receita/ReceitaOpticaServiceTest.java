package com.calycontrol.otica.domain.receita;

import com.calycontrol.otica.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceitaOpticaServiceTest {

    @Mock
    private ReceitaOpticaRepository repository;

    @InjectMocks
    private ReceitaOpticaService service;

    private ReceitaOptica receita;

    @BeforeEach
    void setUp() {
        receita = new ReceitaOptica();
        receita.setValidade(LocalDate.now().plusYears(1));
        receita.setDataEmissao(LocalDate.now());
    }

    @Test
    @DisplayName("findAll deve delegar ao repository")
    void findAll_deveDelegarAoRepository() {
        when(repository.findAllComDetalhes()).thenReturn(List.of(receita));
        assertThat(service.findAll()).hasSize(1);
        verify(repository).findAllComDetalhes();
    }

    @Test
    @DisplayName("findByCliente deve retornar receitas do cliente")
    void findByCliente_deveRetornarReceitasDoCliente() {
        when(repository.findByClienteId(1L)).thenReturn(List.of(receita));
        assertThat(service.findByCliente(1L)).hasSize(1);
        verify(repository).findByClienteId(1L);
    }

    @Test
    @DisplayName("findById deve retornar receita quando encontrada")
    void findById_deveRetornar_quandoEncontrada() {
        when(repository.findById(1L)).thenReturn(Optional.of(receita));
        assertThat(service.findById(1L)).isEqualTo(receita);
    }

    @Test
    @DisplayName("findById deve lançar BusinessException quando não encontrada")
    void findById_deveLancar_quandoNaoEncontrada() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("save deve persistir a receita")
    void save_devePersistir() {
        when(repository.save(receita)).thenReturn(receita);
        assertThat(service.save(receita)).isEqualTo(receita);
        verify(repository).save(receita);
    }

    @Test
    @DisplayName("delete deve remover a receita existente")
    void delete_deveRemover_quandoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(receita));
        service.delete(1L);
        verify(repository).delete(receita);
    }

    @Test
    @DisplayName("delete deve lançar BusinessException quando não existe")
    void delete_deveLancar_quandoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(BusinessException.class);
        verify(repository, never()).delete(any());
    }
}
