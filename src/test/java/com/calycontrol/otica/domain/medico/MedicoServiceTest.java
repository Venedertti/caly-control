package com.calycontrol.otica.domain.medico;

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
class MedicoServiceTest {

    @Mock
    private MedicoRepository repository;

    @InjectMocks
    private MedicoService service;

    private Medico medico;

    @BeforeEach
    void setUp() {
        medico = new Medico();
        medico.setId(1L);
        medico.setNome("Dr. João");
        medico.setCrm("CRM-12345");
        medico.setAtivo(true);
    }

    @Test
    @DisplayName("findAll deve retornar médicos ativos")
    void findAll_deveRetornarMedicosAtivos() {
        when(repository.findByAtivoTrueOrderByNome()).thenReturn(List.of(medico));
        assertThat(service.findAll()).hasSize(1);
        verify(repository).findByAtivoTrueOrderByNome();
    }

    @Test
    @DisplayName("findById deve retornar médico quando encontrado")
    void findById_deveRetornar_quandoEncontrado() {
        when(repository.findById(1L)).thenReturn(Optional.of(medico));
        assertThat(service.findById(1L)).isEqualTo(medico);
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
    @DisplayName("save deve persistir quando CRM não está duplicado")
    void save_devePersistir_quandoCrmNaoDuplicado() {
        when(repository.findByCrm("CRM-12345")).thenReturn(Optional.empty());
        when(repository.save(medico)).thenReturn(medico);
        assertThat(service.save(medico)).isEqualTo(medico);
    }

    @Test
    @DisplayName("save deve lançar BusinessException quando CRM pertence a outro médico")
    void save_deveLancar_quandoCrmDuplicado() {
        Medico outro = new Medico();
        outro.setId(2L);
        outro.setCrm("CRM-12345");
        when(repository.findByCrm("CRM-12345")).thenReturn(Optional.of(outro));
        assertThatThrownBy(() -> service.save(medico))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CRM");
    }

    @Test
    @DisplayName("save deve permitir atualizar o próprio médico")
    void save_devePermitirAtualizacaoDoProprioMedico() {
        when(repository.findByCrm("CRM-12345")).thenReturn(Optional.of(medico));
        when(repository.save(medico)).thenReturn(medico);
        assertThatNoException().isThrownBy(() -> service.save(medico));
    }

    @Test
    @DisplayName("desativar deve marcar médico como inativo")
    void desativar_deveMarcarcomo_inativo() {
        when(repository.findById(1L)).thenReturn(Optional.of(medico));
        service.desativar(1L);
        assertThat(medico.isAtivo()).isFalse();
        verify(repository).save(medico);
    }

    @Test
    @DisplayName("desativar deve lançar BusinessException quando médico não existe")
    void desativar_deveLancar_quandoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.desativar(99L))
                .isInstanceOf(BusinessException.class);
    }
}
