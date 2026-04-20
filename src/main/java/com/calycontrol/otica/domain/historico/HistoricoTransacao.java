package com.calycontrol.otica.domain.historico;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Snapshot desnormalizado de qualquer evento financeiro ou de estoque.
 * Cada linha é autocontida — o frontend lê sem joins adicionais.
 *
 * Valores de tipo/subtipo:
 *   tipo    → VENDA | ORDEM_SERVICO | MOVIMENTACAO_ESTOQUE
 *   subtipo → FormaPagamento (venda) | status OS | ENTRADA/SAIDA/AJUSTE (estoque)
 */
@Entity
@Table(name = "historico_transacao")
@Getter
@Setter
public class HistoricoTransacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Classificação ────────────────────────────────────────────────────────
    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(length = 30)
    private String subtipo;

    // ── Referência à tabela de origem ────────────────────────────────────────
    @Column(name = "origem_tabela", nullable = false, length = 30)
    private String origemTabela;

    @Column(name = "origem_id", nullable = false)
    private Long origemId;

    @Column(name = "numero_referencia", length = 20)
    private String numeroReferencia;

    // ── Temporal ─────────────────────────────────────────────────────────────
    @Column(name = "data_transacao", nullable = false)
    private LocalDateTime dataTransacao;

    @Column(name = "data_registrado_em", nullable = false)
    private LocalDateTime dataRegistradoEm = LocalDateTime.now();

    // ── Cliente (desnormalizado) ──────────────────────────────────────────────
    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "cliente_nome", length = 100)
    private String clienteNome;

    @Column(name = "cliente_cpf", length = 14)
    private String clienteCpf;

    @Column(name = "cliente_telefone", length = 20)
    private String clienteTelefone;

    @Column(name = "cliente_email", length = 150)
    private String clienteEmail;

    // ── Operador responsável (desnormalizado) ─────────────────────────────────
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "usuario_nome", nullable = false, length = 100)
    private String usuarioNome;

    @Column(name = "usuario_perfil", nullable = false, length = 20)
    private String usuarioPerfil;

    // ── Valores financeiros ───────────────────────────────────────────────────
    @Column(name = "valor_bruto", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorBruto = BigDecimal.ZERO;

    @Column(name = "valor_desconto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDescontoTotal = BigDecimal.ZERO;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "valor_pago", precision = 10, scale = 2)
    private BigDecimal valorPago;

    // ── Pagamento ─────────────────────────────────────────────────────────────
    @Column(name = "forma_pagamento", length = 20)
    private String formaPagamento;

    @Column
    private Integer parcelas;

    // ── Itens (desnormalizado) ────────────────────────────────────────────────
    @Column(name = "itens_resumo", columnDefinition = "VARCHAR(MAX)")
    private String itensResumo;

    @Column(name = "itens_quantidade_total", nullable = false)
    private int itensQuantidadeTotal = 0;

    @Column(name = "itens_count", nullable = false)
    private int itensCount = 0;

    // ── Receita / OS (desnormalizado) ─────────────────────────────────────────
    @Column(name = "receita_id")
    private Long receitaId;

    @Column(name = "medico_nome", length = 100)
    private String medicoNome;

    @Column(name = "medico_crm", length = 20)
    private String medicoCrm;

    @Column(name = "receita_data_emissao")
    private LocalDate receitaDataEmissao;

    @Column(name = "receita_validade")
    private LocalDate receitaValidade;

    @Column(name = "os_data_previsao")
    private LocalDate osDataPrevisao;

    @Column(name = "os_data_entrega")
    private LocalDate osDataEntrega;

    @Column(name = "os_status", length = 20)
    private String osStatus;

    // ── Produto — movimentação de estoque ─────────────────────────────────────
    @Column(name = "produto_id")
    private Long produtoId;

    @Column(name = "produto_codigo", length = 50)
    private String produtoCodigo;

    @Column(name = "produto_descricao", length = 200)
    private String produtoDescricao;

    @Column(name = "produto_tipo", length = 20)
    private String produtoTipo;

    @Column(name = "produto_marca", length = 80)
    private String produtoMarca;

    @Column(name = "produto_fornecedor", length = 150)
    private String produtoFornecedor;

    @Column(name = "movimentacao_quantidade")
    private Integer movimentacaoQuantidade;

    // ── Campo livre ───────────────────────────────────────────────────────────
    @Column(length = 500)
    private String observacoes;
}
