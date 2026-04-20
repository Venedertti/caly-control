package com.calycontrol.otica.domain.venda;

import com.calycontrol.otica.domain.cliente.Cliente;
import com.calycontrol.otica.domain.historico.HistoricoService;
import com.calycontrol.otica.domain.historico.HistoricoTransacao;
import com.calycontrol.otica.domain.produto.Produto;
import com.calycontrol.otica.domain.usuario.Usuario;
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
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepository;

    @Mock
    private HistoricoService historicoService;

    @InjectMocks
    private VendaService service;

    private Venda venda;
    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = new Produto();
        produto.setId(1L);
        produto.setDescricao("Armação Titanium");
        produto.setEstoqueAtual(5);

        VendaProduto item = new VendaProduto();
        item.setProduto(produto);
        item.setQuantidade(2);
        item.setPrecoUnitario(new BigDecimal("299.90"));
        item.setDesconto(BigDecimal.ZERO);

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Vendedor");

        venda = new Venda();
        venda.setCliente(cliente);
        venda.setUsuario(usuario);
        venda.setFormaPagamento(FormaPagamento.PIX);
        venda.setItens(new ArrayList<>(List.of(item)));
    }

    @Test
    @DisplayName("registrar deve lançar BusinessException quando não há itens")
    void registrar_deveLancar_quandoSemItens() {
        venda.setItens(new ArrayList<>());

        assertThatThrownBy(() -> service.registrar(venda))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("item");
    }

    @Test
    @DisplayName("registrar deve lançar BusinessException quando estoque insuficiente")
    void registrar_deveLancar_quandoEstoqueInsuficiente() {
        produto.setEstoqueAtual(1);
        venda.getItens().get(0).setQuantidade(5);

        when(vendaRepository.existsByNumeroVenda(any())).thenReturn(false);

        assertThatThrownBy(() -> service.registrar(venda))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    @DisplayName("registrar deve persistir venda, baixar estoque e registrar histórico")
    void registrar_devePersistirEBaixarEstoque() {
        when(vendaRepository.existsByNumeroVenda(any())).thenReturn(false);
        when(vendaRepository.save(venda)).thenReturn(venda);
        when(historicoService.registrarVenda(venda)).thenReturn(new HistoricoTransacao());

        Venda resultado = service.registrar(venda);

        assertThat(produto.getEstoqueAtual()).isEqualTo(3);
        assertThat(resultado.getNumeroVenda()).startsWith("VDA-");
        verify(vendaRepository).save(venda);
        verify(historicoService).registrarVenda(venda);
    }

    @Test
    @DisplayName("registrar deve recalcular o total corretamente")
    void registrar_deveRecalcularTotal() {
        when(vendaRepository.existsByNumeroVenda(any())).thenReturn(false);
        when(vendaRepository.save(venda)).thenReturn(venda);
        when(historicoService.registrarVenda(venda)).thenReturn(new HistoricoTransacao());

        service.registrar(venda);

        // 2 x 299.90 - 0.00 = 599.80
        assertThat(venda.getValorTotal()).isEqualByComparingTo(new BigDecimal("599.80"));
    }

    @Test
    @DisplayName("registrar deve vincular cada item à venda pai")
    void registrar_deveVincularItensPai() {
        when(vendaRepository.existsByNumeroVenda(any())).thenReturn(false);
        when(vendaRepository.save(venda)).thenReturn(venda);
        when(historicoService.registrarVenda(venda)).thenReturn(new HistoricoTransacao());

        service.registrar(venda);

        venda.getItens().forEach(item -> assertThat(item.getVenda()).isEqualTo(venda));
    }

    @Test
    @DisplayName("findById deve lançar BusinessException quando venda não existe")
    void findById_deveLancar_quandoNaoExiste() {
        when(vendaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("findById deve retornar venda quando encontrada")
    void findById_deveRetornar_quandoEncontrado() {
        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));

        assertThat(service.findById(1L)).isEqualTo(venda);
    }
}
