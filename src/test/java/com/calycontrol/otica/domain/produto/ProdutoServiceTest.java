package com.calycontrol.otica.domain.produto;

import com.calycontrol.otica.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoService service;

    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = new Produto();
        produto.setId(1L);
        produto.setCodigo("ARM-001");
        produto.setDescricao("Armação Titanium");
        produto.setTipo(TipoProduto.ARMACAO);
        produto.setPrecoVenda(new BigDecimal("299.90"));
        produto.setEstoqueAtual(10);
        produto.setEstoqueMinimo(2);
        produto.setAtivo(true);
    }

    @Test
    @DisplayName("findAll deve retornar apenas produtos ativos")
    void findAll_deveRetornarProdutosAtivos() {
        when(repository.findByAtivoTrueOrderByDescricao()).thenReturn(List.of(produto));

        assertThat(service.findAll()).hasSize(1).contains(produto);
        verify(repository).findByAtivoTrueOrderByDescricao();
    }

    @Test
    @DisplayName("buscar com termo vazio deve retornar todos os ativos")
    void buscar_comTermoVazio_deveRetornarTodos() {
        when(repository.findByAtivoTrueOrderByDescricao()).thenReturn(List.of(produto));

        assertThat(service.buscar("")).hasSize(1);
        assertThat(service.buscar(null)).hasSize(1);
    }

    @Test
    @DisplayName("buscar com termo deve delegar ao repository com trim")
    void buscar_comTermo_deveDelegarComTrim() {
        when(repository.buscar("ARM")).thenReturn(List.of(produto));

        service.buscar("  ARM  ");

        verify(repository).buscar("ARM");
    }

    @Test
    @DisplayName("findEstoqueBaixo deve delegar ao repository")
    void findEstoqueBaixo_deveDelegarAoRepository() {
        when(repository.findEstoqueBaixo()).thenReturn(List.of(produto));

        List<Produto> result = service.findEstoqueBaixo();

        assertThat(result).hasSize(1);
        verify(repository).findEstoqueBaixo();
    }

    @Test
    @DisplayName("findById deve retornar produto quando encontrado")
    void findById_deveRetornar_quandoEncontrado() {
        when(repository.findById(1L)).thenReturn(Optional.of(produto));

        assertThat(service.findById(1L)).isEqualTo(produto);
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
    @DisplayName("save deve persistir quando código não está duplicado")
    void save_devePersistir_quandoCodigoNaoDuplicado() {
        when(repository.findByCodigo("ARM-001")).thenReturn(Optional.empty());
        when(repository.save(produto)).thenReturn(produto);

        assertThat(service.save(produto)).isEqualTo(produto);
        verify(repository).save(produto);
    }

    @Test
    @DisplayName("save deve lançar BusinessException quando código pertence a outro produto")
    void save_deveLancar_quandoCodigoDuplicadoDeOutroProduto() {
        Produto outro = new Produto();
        outro.setId(2L);
        outro.setCodigo("ARM-001");

        when(repository.findByCodigo("ARM-001")).thenReturn(Optional.of(outro));

        assertThatThrownBy(() -> service.save(produto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Código");
    }

    @Test
    @DisplayName("save deve permitir atualizar o próprio produto sem conflito de código")
    void save_devePermitirAtualizacaoDoProproProduto() {
        when(repository.findByCodigo("ARM-001")).thenReturn(Optional.of(produto));
        when(repository.save(produto)).thenReturn(produto);

        assertThat(service.save(produto)).isEqualTo(produto);
    }

    @Test
    @DisplayName("desativar deve marcar produto como inativo")
    void desativar_deveMarcarcomo_inativo() {
        when(repository.findById(1L)).thenReturn(Optional.of(produto));

        service.desativar(1L);

        assertThat(produto.isAtivo()).isFalse();
        verify(repository).save(produto);
    }
}
