package com.calycontrol.otica.domain.cliente;

import com.calycontrol.otica.domain.historico.HistoricoService;
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
class ClienteServiceTest {

    @Mock
    private ClienteRepository repository;

    @Mock
    private HistoricoService historicoService;

    @InjectMocks
    private ClienteService service;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setCpf("123.456.789-00");
        cliente.setAtivo(true);
    }

    @Test
    @DisplayName("findAll deve retornar lista de clientes ativos")
    void findAll_deveRetornarClientesAtivos() {
        when(repository.findByAtivoTrueOrderByNome()).thenReturn(List.of(cliente));

        List<Cliente> result = service.findAll();

        assertThat(result).hasSize(1).contains(cliente);
        verify(repository).findByAtivoTrueOrderByNome();
    }

    @Test
    @DisplayName("buscar com termo vazio deve retornar todos os clientes ativos")
    void buscar_comTermoVazio_deveRetornarTodos() {
        when(repository.findByAtivoTrueOrderByNome()).thenReturn(List.of(cliente));

        assertThat(service.buscar("")).hasSize(1);
        assertThat(service.buscar(null)).hasSize(1);
        assertThat(service.buscar("   ")).hasSize(1);
    }

    @Test
    @DisplayName("buscar com termo deve chamar buscar no repository")
    void buscar_comTermo_deveDelegarAoRepository() {
        when(repository.buscar("João")).thenReturn(List.of(cliente));

        List<Cliente> result = service.buscar("João");

        assertThat(result).hasSize(1);
        verify(repository).buscar("João");
    }

    @Test
    @DisplayName("buscar deve fazer trim no termo antes de delegar")
    void buscar_deveFazerTrimNoTermo() {
        when(repository.buscar("João")).thenReturn(List.of(cliente));

        service.buscar("  João  ");

        verify(repository).buscar("João");
    }

    @Test
    @DisplayName("findById deve retornar cliente quando encontrado")
    void findById_deveRetornarCliente_quandoEncontrado() {
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThat(service.findById(1L)).isEqualTo(cliente);
    }

    @Test
    @DisplayName("findById deve lançar BusinessException quando não encontrado")
    void findById_deveLancarBusinessException_quandoNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("save deve persistir cliente quando CPF não está duplicado")
    void save_devePersistir_quandoCpfNaoDuplicado() {
        when(repository.findByCpf(cliente.getCpf())).thenReturn(Optional.empty());
        when(repository.save(cliente)).thenReturn(cliente);

        assertThat(service.save(cliente)).isEqualTo(cliente);
        verify(repository).save(cliente);
    }

    @Test
    @DisplayName("save deve lançar BusinessException quando CPF pertence a outro cliente")
    void save_deveLancar_quandoCpfDuplicadoDeOutroCliente() {
        Cliente outro = new Cliente();
        outro.setId(2L);
        outro.setCpf(cliente.getCpf());

        when(repository.findByCpf(cliente.getCpf())).thenReturn(Optional.of(outro));

        assertThatThrownBy(() -> service.save(cliente))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF");
    }

    @Test
    @DisplayName("save deve permitir atualizar o próprio cliente sem conflito de CPF")
    void save_devePermitirAtualizacaoDoProprioCliente() {
        when(repository.findByCpf(cliente.getCpf())).thenReturn(Optional.of(cliente));
        when(repository.save(cliente)).thenReturn(cliente);

        assertThat(service.save(cliente)).isEqualTo(cliente);
    }

    @Test
    @DisplayName("save deve aceitar cliente sem CPF")
    void save_deveAceitarClienteSemCpf() {
        cliente.setCpf(null);
        when(repository.save(cliente)).thenReturn(cliente);

        assertThat(service.save(cliente)).isEqualTo(cliente);
        verify(repository, never()).findByCpf(any());
    }

    @Test
    @DisplayName("desativar deve marcar cliente como inativo")
    void desativar_deveMarcarclienteComoInativo() {
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));

        service.desativar(1L);

        assertThat(cliente.isAtivo()).isFalse();
        verify(repository).save(cliente);
    }

    @Test
    @DisplayName("desativar deve lançar BusinessException quando cliente não existe")
    void desativar_deveLancar_quandoClienteNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.desativar(99L))
                .isInstanceOf(BusinessException.class);
    }

    // ── LGPD Art. 18, IV — direito à eliminação ─────────────────────────────

    @Test
    @DisplayName("anonimizar deve limpar dados pessoais e marcar o registro")
    void anonimizar_deveLimparDadosPessoais() {
        cliente.setTelefone("11999999999");
        cliente.setEmail("joao@exemplo.com");
        cliente.setEndereco("Rua X, 123");
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));

        service.anonimizar(1L);

        assertThat(cliente.getCpf()).isNull();
        assertThat(cliente.getTelefone()).isNull();
        assertThat(cliente.getEmail()).isNull();
        assertThat(cliente.getEndereco()).isNull();
        assertThat(cliente.getCep()).isNull();
        assertThat(cliente.getDataNascimento()).isNull();
        assertThat(cliente.getNome()).isEqualTo("Cliente Anonimizado #1");
        assertThat(cliente.isAnonimizado()).isTrue();
        assertThat(cliente.isAtivo()).isFalse();
        assertThat(cliente.getDataAnonimizacao()).isNotNull();
    }

    @Test
    @DisplayName("anonimizar deve propagar a limpeza para o histórico")
    void anonimizar_devePropagaParaHistorico() {
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));

        service.anonimizar(1L);

        verify(historicoService).anonimizarCliente(1L);
    }

    @Test
    @DisplayName("anonimizar deve lançar BusinessException quando já anonimizado")
    void anonimizar_deveLancar_quandoJaAnonimizado() {
        cliente.setAnonimizado(true);
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> service.anonimizar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("anonimizado");
        verify(historicoService, never()).anonimizarCliente(any());
    }

    @Test
    @DisplayName("save não deve aceitar cliente já anonimizado")
    void save_deveLancar_quandoClienteAnonimizado() {
        cliente.setAnonimizado(true);

        assertThatThrownBy(() -> service.save(cliente))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("anonimizado");
    }
}
