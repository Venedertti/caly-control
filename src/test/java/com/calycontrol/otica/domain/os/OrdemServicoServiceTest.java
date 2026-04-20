package com.calycontrol.otica.domain.os;

import com.calycontrol.otica.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @Mock
    private OrdemServicoRepository repository;

    @InjectMocks
    private OrdemServicoService service;

    private OrdemServico os;
    private OsProduto item;

    @BeforeEach
    void setUp() {
        item = new OsProduto();
        item.setQuantidade(1);
        item.setPrecoUnitario(new BigDecimal("150.00"));
        item.setDesconto(BigDecimal.ZERO);

        os = new OrdemServico();
        os.setId(1L);
        os.setStatus(StatusOS.ABERTA);
        os.setItens(new ArrayList<>(List.of(item)));
    }

    @Test
    @DisplayName("findById deve retornar OS quando encontrada")
    void findById_deveRetornar_quandoEncontrada() {
        when(repository.findById(1L)).thenReturn(Optional.of(os));
        assertThat(service.findById(1L)).isEqualTo(os);
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
    @DisplayName("countAbertasEmProducao deve somar ABERTA + EM_PRODUCAO")
    void countAbertasEmProducao_deveSomar() {
        when(repository.countByStatus(StatusOS.ABERTA)).thenReturn(3L);
        when(repository.countByStatus(StatusOS.EM_PRODUCAO)).thenReturn(2L);
        assertThat(service.countAbertasEmProducao()).isEqualTo(5L);
    }

    @Test
    @DisplayName("countProntas deve retornar count de PRONTA")
    void countProntas_deveRetornarCountDeProntas() {
        when(repository.countByStatus(StatusOS.PRONTA)).thenReturn(4L);
        assertThat(service.countProntas()).isEqualTo(4L);
    }

    @Test
    @DisplayName("abrir deve lançar BusinessException quando não há itens")
    void abrir_deveLancar_quandoSemItens() {
        os.setItens(new ArrayList<>());
        assertThatThrownBy(() -> service.abrir(os))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("produto");
    }

    @Test
    @DisplayName("abrir deve gerar número, vincular itens e persistir")
    void abrir_deveGerarNumeroEPersistir() {
        when(repository.count()).thenReturn(0L);
        when(repository.existsByNumeroOs(any())).thenReturn(false);
        when(repository.save(os)).thenReturn(os);

        OrdemServico resultado = service.abrir(os);

        assertThat(resultado.getNumeroOs()).startsWith("OS-");
        assertThat(item.getOs()).isEqualTo(os);
        assertThat(os.getValorTotal()).isEqualByComparingTo(new BigDecimal("150.00"));
        verify(repository).save(os);
    }

    @Test
    @DisplayName("avancarStatus ABERTA deve ir para EM_PRODUCAO")
    void avancarStatus_deAbertaParaEmProducao() {
        when(repository.findById(1L)).thenReturn(Optional.of(os));

        service.avancarStatus(1L);

        assertThat(os.getStatus()).isEqualTo(StatusOS.EM_PRODUCAO);
        verify(repository).save(os);
    }

    @Test
    @DisplayName("avancarStatus deve lançar quando status não permite avanço")
    void avancarStatus_deveLancar_quandoStatusNaoPermite() {
        os.setStatus(StatusOS.CANCELADA);
        when(repository.findById(1L)).thenReturn(Optional.of(os));

        assertThatThrownBy(() -> service.avancarStatus(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("cancelar deve mudar status para CANCELADA")
    void cancelar_deveMudarParaCancelada() {
        when(repository.findById(1L)).thenReturn(Optional.of(os));

        service.cancelar(1L);

        assertThat(os.getStatus()).isEqualTo(StatusOS.CANCELADA);
        verify(repository).save(os);
    }

    @Test
    @DisplayName("cancelar deve lançar quando OS já foi entregue")
    void cancelar_deveLancar_quandoJaEntregue() {
        os.setStatus(StatusOS.ENTREGUE);
        when(repository.findById(1L)).thenReturn(Optional.of(os));

        assertThatThrownBy(() -> service.cancelar(1L))
                .isInstanceOf(BusinessException.class);
    }
}
