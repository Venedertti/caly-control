package com.calycontrol.otica.domain.venda;

import com.calycontrol.otica.domain.historico.HistoricoService;
import com.calycontrol.otica.domain.produto.Produto;
import com.calycontrol.otica.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final VendaRepository vendaRepository;
    private final HistoricoService historicoService;

    private static final DateTimeFormatter NUM_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public List<Venda> findAll() {
        return vendaRepository.findAll();
    }

    public List<Venda> findUltimas20() {
        return vendaRepository.findTop20ByOrderByDataVendaDesc();
    }

    public Venda findById(Long id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Venda não encontrada: " + id));
    }

    /**
     * Persiste a venda e, na mesma transação, insere a linha no histórico.
     * Qualquer falha em qualquer etapa reverte todo o trabalho.
     */
    @Transactional
    public Venda registrar(Venda venda) {
        if (venda.getItens().isEmpty()) {
            throw new BusinessException("A venda deve ter pelo menos um item.");
        }

        // Gera número único antes de salvar
        venda.setNumeroVenda(gerarNumeroVenda());

        // Vincula cada item à venda pai (necessário para o mapeamento bidirecional)
        venda.getItens().forEach(item -> item.setVenda(venda));

        // Garante que o valor_total está correto
        venda.recalcularTotal();

        // Baixa de estoque — mesma transação
        venda.getItens().forEach(item -> {
            Produto p = item.getProduto();
            int novoEstoque = p.getEstoqueAtual() - item.getQuantidade();
            if (novoEstoque < 0) {
                throw new BusinessException(
                    "Estoque insuficiente para o produto: " + p.getDescricao() +
                    " (disponível: " + p.getEstoqueAtual() + ")");
            }
            p.setEstoqueAtual(novoEstoque);
        });

        Venda salva = vendaRepository.save(venda);

        // Insere a linha desnormalizada no histórico — mesma transação
        historicoService.registrarVenda(salva);

        return salva;
    }

    private String gerarNumeroVenda() {
        String numero;
        int tentativas = 0;
        do {
            numero = "VDA-" + LocalDateTime.now().format(NUM_FMT);
            tentativas++;
            if (tentativas > 10) {
                throw new BusinessException("Não foi possível gerar número único para a venda.");
            }
        } while (vendaRepository.existsByNumeroVenda(numero));
        return numero;
    }
}
