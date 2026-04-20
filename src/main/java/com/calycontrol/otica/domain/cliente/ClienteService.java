package com.calycontrol.otica.domain.cliente;

import com.calycontrol.otica.domain.historico.HistoricoService;
import com.calycontrol.otica.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;
    private final HistoricoService historicoService;

    public List<Cliente> findAll() {
        return repository.findByAtivoTrueOrderByNome();
    }

    public List<Cliente> buscar(String termo) {
        if (!StringUtils.hasText(termo)) {
            return findAll();
        }
        return repository.buscar(termo.trim());
    }

    public Cliente findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado: " + id));
    }

    @Transactional
    public Cliente save(Cliente cliente) {
        if (cliente.isAnonimizado()) {
            throw new BusinessException("Não é possível editar um cliente anonimizado.");
        }
        if (StringUtils.hasText(cliente.getCpf())) {
            repository.findByCpf(cliente.getCpf())
                    .filter(c -> !c.getId().equals(cliente.getId()))
                    .ifPresent(c -> { throw new BusinessException("CPF já cadastrado para outro cliente."); });
        }
        return repository.save(cliente);
    }

    @Transactional
    public void desativar(Long id) {
        Cliente cliente = findById(id);
        cliente.setAtivo(false);
        repository.save(cliente);
    }

    /**
     * LGPD Art. 18, IV — direito à eliminação dos dados pessoais.
     *
     * Apaga os dados pessoais identificáveis do cliente (CPF, telefone,
     * e-mail, endereço, data de nascimento) e substitui o nome por um
     * pseudônimo. Propaga a anonimização para os registros desnormalizados
     * em historico_transacao.
     *
     * O registro físico do cliente é preservado para manter a integridade
     * referencial de vendas, receitas e ordens de serviço já concluídas —
     * uma necessidade legal e contábil reconhecida pela própria LGPD
     * (Art. 16, II — cumprimento de obrigação legal/regulatória).
     */
    @Transactional
    public void anonimizar(Long id) {
        Cliente cliente = findById(id);
        if (cliente.isAnonimizado()) {
            throw new BusinessException("Cliente já está anonimizado.");
        }

        cliente.setNome("Cliente Anonimizado #" + cliente.getId());
        cliente.setCpf(null);
        cliente.setTelefone(null);
        cliente.setEmail(null);
        cliente.setCep(null);
        cliente.setEndereco(null);
        cliente.setDataNascimento(null);
        cliente.setAtivo(false);
        cliente.setAnonimizado(true);
        cliente.setDataAnonimizacao(LocalDateTime.now());

        repository.save(cliente);
        historicoService.anonimizarCliente(id);
    }
}
