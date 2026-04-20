package com.calycontrol.otica.domain.historico;

import com.calycontrol.otica.domain.venda.Venda;
import com.calycontrol.otica.domain.venda.VendaProduto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final HistoricoTransacaoRepository repository;

    /**
     * Insere uma linha desnormalizada no histórico para a venda informada.
     * Deve ser chamado dentro da mesma transação que persistiu a venda,
     * para garantir rollback conjunto em caso de falha.
     */
    public HistoricoTransacao registrarVenda(Venda venda) {
        HistoricoTransacao h = new HistoricoTransacao();

        h.setTipo("VENDA");
        h.setSubtipo(venda.getFormaPagamento().name());

        h.setOrigemTabela("venda");
        h.setOrigemId(venda.getId());
        h.setNumeroReferencia(venda.getNumeroVenda());

        h.setDataTransacao(venda.getDataVenda());

        // Cliente desnormalizado
        var cliente = venda.getCliente();
        h.setClienteId(cliente.getId());
        h.setClienteNome(cliente.getNome());
        h.setClienteCpf(cliente.getCpf());
        h.setClienteTelefone(cliente.getTelefone());
        h.setClienteEmail(cliente.getEmail());

        // Operador desnormalizado
        var usuario = venda.getUsuario();
        h.setUsuarioId(usuario.getId());
        h.setUsuarioNome(usuario.getNome());
        h.setUsuarioPerfil(usuario.getPerfil().name());

        // Valores financeiros
        List<VendaProduto> itens = venda.getItens();

        BigDecimal valorBruto = itens.stream()
                .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorDesconto = itens.stream()
                .map(VendaProduto::getDesconto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        h.setValorBruto(valorBruto);
        h.setValorDescontoTotal(valorDesconto);
        h.setValorTotal(venda.getValorTotal());

        // Pagamento
        h.setFormaPagamento(venda.getFormaPagamento().name());
        h.setParcelas(venda.getParcelas());

        // Itens — resumo desnormalizado, ordenado pelo nome do produto
        String itensResumo = itens.stream()
                .sorted(Comparator.comparing(i -> i.getProduto().getDescricao()))
                .map(i -> {
                    String linha = i.getProduto().getDescricao()
                            + " x" + i.getQuantidade()
                            + " @ R$ " + formatDecimal(i.getPrecoUnitario());
                    if (i.getDesconto() != null && i.getDesconto().compareTo(BigDecimal.ZERO) > 0) {
                        linha += " (desc. R$ " + formatDecimal(i.getDesconto()) + ")";
                    }
                    return linha;
                })
                .collect(Collectors.joining(" | "));

        int qtdTotal = itens.stream().mapToInt(VendaProduto::getQuantidade).sum();

        h.setItensResumo(itensResumo);
        h.setItensQuantidadeTotal(qtdTotal);
        h.setItensCount(itens.size());

        return repository.save(h);
    }

    private String formatDecimal(BigDecimal value) {
        if (value == null) return "0,00";
        return String.format("%.2f", value).replace('.', ',');
    }

    /**
     * Anonimiza todos os registros de histórico associados ao cliente
     * (direito ao esquecimento — LGPD Art. 18, IV). Preserva campos
     * financeiros, apenas remove dados pessoais identificáveis.
     *
     * @return quantidade de linhas atualizadas.
     */
    public int anonimizarCliente(Long clienteId) {
        return repository.anonimizarPorCliente(clienteId);
    }
}
