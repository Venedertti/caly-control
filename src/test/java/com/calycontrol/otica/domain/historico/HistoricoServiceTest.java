package com.calycontrol.otica.domain.historico;

import com.calycontrol.otica.domain.cliente.Cliente;
import com.calycontrol.otica.domain.produto.Produto;
import com.calycontrol.otica.domain.usuario.PerfilUsuario;
import com.calycontrol.otica.domain.usuario.Usuario;
import com.calycontrol.otica.domain.venda.FormaPagamento;
import com.calycontrol.otica.domain.venda.Venda;
import com.calycontrol.otica.domain.venda.VendaProduto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricoServiceTest {

    @Mock
    private HistoricoTransacaoRepository repository;

    @InjectMocks
    private HistoricoService service;

    private Venda venda;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setCpf("123.456.789-00");
        cliente.setTelefone("11999999999");
        cliente.setEmail("joao@exemplo.com");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Vendedor");
        usuario.setPerfil(PerfilUsuario.VENDEDOR);

        Produto produto = new Produto();
        produto.setDescricao("Armação Premium");

        VendaProduto item = new VendaProduto();
        item.setQuantidade(2);
        item.setPrecoUnitario(new BigDecimal("299.90"));
        item.setDesconto(new BigDecimal("10.00"));
        item.setProduto(produto);

        venda = new Venda();
        venda.setId(10L);
        venda.setNumeroVenda("VDA-20260417120000");
        venda.setCliente(cliente);
        venda.setUsuario(usuario);
        venda.setFormaPagamento(FormaPagamento.PIX);
        venda.setParcelas(1);
        venda.setDataVenda(LocalDateTime.now());
        venda.setItens(new ArrayList<>(List.of(item)));
        venda.recalcularTotal();
    }

    @Test
    @DisplayName("registrarVenda deve persistir snapshot com dados corretos")
    void registrarVenda_devePersistirSnapshot() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HistoricoTransacao h = service.registrarVenda(venda);

        assertThat(h.getTipo()).isEqualTo("VENDA");
        assertThat(h.getSubtipo()).isEqualTo("PIX");
        assertThat(h.getOrigemTabela()).isEqualTo("venda");
        assertThat(h.getOrigemId()).isEqualTo(10L);
        assertThat(h.getNumeroReferencia()).isEqualTo("VDA-20260417120000");
    }

    @Test
    @DisplayName("registrarVenda deve desnormalizar dados do cliente")
    void registrarVenda_deveDesnormalizarCliente() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HistoricoTransacao h = service.registrarVenda(venda);

        assertThat(h.getClienteId()).isEqualTo(1L);
        assertThat(h.getClienteNome()).isEqualTo("João Silva");
        assertThat(h.getClienteCpf()).isEqualTo("123.456.789-00");
    }

    @Test
    @DisplayName("registrarVenda deve calcular valores financeiros corretamente")
    void registrarVenda_deveCalcularValoresFinanceiros() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HistoricoTransacao h = service.registrarVenda(venda);

        // bruto = 299.90 * 2 = 599.80
        assertThat(h.getValorBruto()).isEqualByComparingTo(new BigDecimal("599.80"));
        // desconto total = 10.00
        assertThat(h.getValorDescontoTotal()).isEqualByComparingTo(new BigDecimal("10.00"));
        // total = 599.80 - 10.00 = 589.80
        assertThat(h.getValorTotal()).isEqualByComparingTo(new BigDecimal("589.80"));
    }

    @Test
    @DisplayName("registrarVenda deve montar resumo dos itens")
    void registrarVenda_deveMontarResumoItens() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HistoricoTransacao h = service.registrarVenda(venda);

        assertThat(h.getItensCount()).isEqualTo(1);
        assertThat(h.getItensQuantidadeTotal()).isEqualTo(2);
        assertThat(h.getItensResumo()).contains("Armação Premium");
    }

    @Test
    @DisplayName("anonimizarCliente deve delegar ao repository")
    void anonimizarCliente_deveDelegarAoRepository() {
        when(repository.anonimizarPorCliente(5L)).thenReturn(3);

        int resultado = service.anonimizarCliente(5L);

        assertThat(resultado).isEqualTo(3);
        verify(repository).anonimizarPorCliente(5L);
    }

    @Test
    @DisplayName("registrarVenda deve salvar via repository")
    void registrarVenda_deveSalvarViaRepository() {
        ArgumentCaptor<HistoricoTransacao> captor = ArgumentCaptor.forClass(HistoricoTransacao.class);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.registrarVenda(venda);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getFormaPagamento()).isEqualTo("PIX");
    }
}
