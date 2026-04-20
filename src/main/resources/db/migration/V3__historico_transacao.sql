-- ============================================================
-- V3 - Tabela de histórico de transações (desnormalizada)
--      e procedure de população
-- ============================================================
-- Propósito: visão imutável (append / full-refresh) de todos
--            os eventos financeiros e de estoque do sistema.
--            Cada linha é autocontida — o frontend lê esta
--            tabela sem precisar de joins adicionais.
-- ============================================================


-- ------------------------------------------------------------
-- 1. TABELA historico_transacao
-- ------------------------------------------------------------
CREATE TABLE historico_transacao (
    id                     BIGINT        IDENTITY(1,1) PRIMARY KEY,

    -- ── Classificação ────────────────────────────────────────
    -- tipo:    VENDA | ORDEM_SERVICO | MOVIMENTACAO_ESTOQUE
    -- subtipo: forma_pagamento (venda) | status da OS | ENTRADA/SAIDA/AJUSTE (estoque)
    tipo                   VARCHAR(30)   NOT NULL,
    subtipo                VARCHAR(30)   NULL,

    -- ── Referência à tabela de origem ────────────────────────
    origem_tabela          VARCHAR(30)   NOT NULL,  -- 'venda' | 'ordem_servico' | 'movimentacao_estoque'
    origem_id              BIGINT        NOT NULL,  -- PK do registro original
    numero_referencia      VARCHAR(20)   NULL,      -- numero_venda ou numero_os (para exibição e busca)

    -- ── Temporal ─────────────────────────────────────────────
    data_transacao         DATETIME2     NOT NULL,  -- data do evento de negócio
    data_registrado_em     DATETIME2     NOT NULL   DEFAULT SYSDATETIME(),

    -- ── Cliente (desnormalizado) ──────────────────────────────
    cliente_id             BIGINT        NULL,
    cliente_nome           VARCHAR(100)  NULL,
    cliente_cpf            VARCHAR(14)   NULL,
    cliente_telefone       VARCHAR(20)   NULL,
    cliente_email          VARCHAR(150)  NULL,

    -- ── Operador responsável (desnormalizado) ─────────────────
    usuario_id             BIGINT        NOT NULL,
    usuario_nome           VARCHAR(100)  NOT NULL,
    usuario_perfil         VARCHAR(20)   NOT NULL,

    -- ── Valores financeiros ───────────────────────────────────
    valor_bruto            DECIMAL(10,2) NOT NULL   DEFAULT 0,  -- soma de (preco_unitario * quantidade)
    valor_desconto_total   DECIMAL(10,2) NOT NULL   DEFAULT 0,  -- soma de todos os descontos dos itens
    valor_total            DECIMAL(10,2) NOT NULL   DEFAULT 0,  -- valor_bruto - valor_desconto_total
    valor_pago             DECIMAL(10,2) NULL,                  -- apenas OS (pode ser pago parcialmente)

    -- ── Pagamento (vendas e OS) ───────────────────────────────
    forma_pagamento        VARCHAR(20)   NULL,       -- DINHEIRO | CARTAO_CREDITO | CARTAO_DEBITO | PIX | CONVENIO
    parcelas               INT           NULL,

    -- ── Resumo dos itens (desnormalizado, pronto para exibição) ─
    -- Formato: "Descrição xQtd @ R$ unitário; ..."
    -- Ex:  "Armação Ray-Ban RB5154 x1 @ R$ 380,00; Lente Hoya Sync III x2 @ R$ 280,00"
    itens_resumo           VARCHAR(MAX)  NULL,
    itens_quantidade_total INT           NOT NULL   DEFAULT 0,
    itens_count            INT           NOT NULL   DEFAULT 0,  -- número de linhas distintas de produto

    -- ── Receita / dados da OS (desnormalizado) ────────────────
    receita_id             BIGINT        NULL,
    medico_nome            VARCHAR(100)  NULL,
    medico_crm             VARCHAR(20)   NULL,
    receita_data_emissao   DATE          NULL,
    receita_validade       DATE          NULL,
    os_data_previsao       DATE          NULL,
    os_data_entrega        DATE          NULL,
    os_status              VARCHAR(20)   NULL,       -- ABERTA | EM_PRODUCAO | PRONTA | ENTREGUE | CANCELADA

    -- ── Dados do produto (movimentacao_estoque — 1 produto/linha) ─
    produto_id             BIGINT        NULL,
    produto_codigo         VARCHAR(50)   NULL,
    produto_descricao      VARCHAR(200)  NULL,
    produto_tipo           VARCHAR(20)   NULL,
    produto_marca          VARCHAR(80)   NULL,
    produto_fornecedor     VARCHAR(150)  NULL,
    movimentacao_quantidade INT          NULL,       -- positivo = entrada; negativo = saída/ajuste para baixo

    -- ── Campo livre ───────────────────────────────────────────
    observacoes            VARCHAR(500)  NULL
);

-- Índices para os filtros e ordenações mais comuns no frontend
CREATE INDEX IX_historico_data_transacao  ON historico_transacao (data_transacao DESC);
CREATE INDEX IX_historico_tipo            ON historico_transacao (tipo);
CREATE INDEX IX_historico_origem          ON historico_transacao (origem_tabela, origem_id);
CREATE INDEX IX_historico_cliente_id      ON historico_transacao (cliente_id);
CREATE INDEX IX_historico_usuario_id      ON historico_transacao (usuario_id);
CREATE INDEX IX_historico_numero_ref      ON historico_transacao (numero_referencia);


-- ------------------------------------------------------------
-- 2. PROCEDURE sp_popular_historico_transacao
-- ------------------------------------------------------------
-- Estratégia: full-refresh (TRUNCATE + reinsert).
--   • Simples, determinístico e sem drift de dados.
--   • Execute após qualquer operação que altere vendas, OS
--     ou movimentações. Em produção, agende via SQL Agent ou
--     chame a partir da camada de serviço após commits.
-- ------------------------------------------------------------
GO

CREATE OR ALTER PROCEDURE sp_popular_historico_transacao
AS
BEGIN
    SET NOCOUNT ON;

    -- ── Limpa o histórico para reprocessamento completo ───────
    TRUNCATE TABLE historico_transacao;


    -- ══════════════════════════════════════════════════════════
    -- BLOCO 1 — VENDAS
    -- ══════════════════════════════════════════════════════════
    -- Uma linha por venda. Itens agregados em itens_resumo.
    INSERT INTO historico_transacao (
        tipo, subtipo,
        origem_tabela, origem_id, numero_referencia,
        data_transacao, data_registrado_em,
        cliente_id, cliente_nome, cliente_cpf, cliente_telefone, cliente_email,
        usuario_id, usuario_nome, usuario_perfil,
        valor_bruto, valor_desconto_total, valor_total,
        forma_pagamento, parcelas,
        itens_resumo, itens_quantidade_total, itens_count,
        observacoes
    )
    SELECT
        'VENDA'                         AS tipo,
        v.forma_pagamento               AS subtipo,

        'venda'                         AS origem_tabela,
        v.id                            AS origem_id,
        v.numero_venda                  AS numero_referencia,

        v.data_venda                    AS data_transacao,
        SYSDATETIME()                   AS data_registrado_em,

        -- Cliente
        c.id                            AS cliente_id,
        c.nome                          AS cliente_nome,
        c.cpf                           AS cliente_cpf,
        c.telefone                      AS cliente_telefone,
        c.email                         AS cliente_email,

        -- Operador
        u.id                            AS usuario_id,
        u.nome                          AS usuario_nome,
        u.perfil                        AS usuario_perfil,

        -- Valores calculados a partir dos itens
        ISNULL(vi.valor_bruto,       0) AS valor_bruto,
        ISNULL(vi.valor_desconto,    0) AS valor_desconto_total,
        -- Usa o valor_total registrado na venda como fonte de verdade
        v.valor_total                   AS valor_total,

        -- Pagamento
        v.forma_pagamento,
        v.parcelas,

        -- Itens
        vi.itens_resumo,
        ISNULL(vi.qtd_total,         0) AS itens_quantidade_total,
        ISNULL(vi.linhas_count,      0) AS itens_count,

        NULL                            AS observacoes

    FROM venda v
    JOIN cliente c  ON c.id = v.cliente_id
    JOIN usuario u  ON u.id = v.usuario_id
    LEFT JOIN (
        -- Agrega itens de cada venda em uma única linha
        SELECT
            vp.venda_id,
            SUM(vp.preco_unitario * vp.quantidade)  AS valor_bruto,
            SUM(vp.desconto)                        AS valor_desconto,
            SUM(vp.quantidade)                      AS qtd_total,
            COUNT(*)                                AS linhas_count,
            STRING_AGG(
                p.descricao
                + ' x' + CAST(vp.quantidade AS VARCHAR(10))
                + ' @ R$ ' + FORMAT(vp.preco_unitario, 'N2', 'pt-BR')
                + CASE WHEN vp.desconto > 0
                       THEN ' (desc. R$ ' + FORMAT(vp.desconto, 'N2', 'pt-BR') + ')'
                       ELSE '' END,
                ' | '
            ) WITHIN GROUP (ORDER BY p.descricao)  AS itens_resumo
        FROM venda_produto vp
        JOIN produto p ON p.id = vp.produto_id
        GROUP BY vp.venda_id
    ) vi ON vi.venda_id = v.id;


    -- ══════════════════════════════════════════════════════════
    -- BLOCO 2 — ORDENS DE SERVIÇO
    -- ══════════════════════════════════════════════════════════
    -- Uma linha por OS, refletindo o estado atual do status.
    INSERT INTO historico_transacao (
        tipo, subtipo,
        origem_tabela, origem_id, numero_referencia,
        data_transacao, data_registrado_em,
        cliente_id, cliente_nome, cliente_cpf, cliente_telefone, cliente_email,
        usuario_id, usuario_nome, usuario_perfil,
        valor_bruto, valor_desconto_total, valor_total, valor_pago,
        forma_pagamento,
        itens_resumo, itens_quantidade_total, itens_count,
        receita_id, medico_nome, medico_crm,
        receita_data_emissao, receita_validade,
        os_data_previsao, os_data_entrega, os_status,
        observacoes
    )
    SELECT
        'ORDEM_SERVICO'                 AS tipo,
        os.status                       AS subtipo,

        'ordem_servico'                 AS origem_tabela,
        os.id                           AS origem_id,
        os.numero_os                    AS numero_referencia,

        CAST(os.data_abertura AS DATETIME2) AS data_transacao,
        SYSDATETIME()                   AS data_registrado_em,

        -- Cliente
        c.id                            AS cliente_id,
        c.nome                          AS cliente_nome,
        c.cpf                           AS cliente_cpf,
        c.telefone                      AS cliente_telefone,
        c.email                         AS cliente_email,

        -- Operador (quem abriu a OS)
        u.id                            AS usuario_id,
        u.nome                          AS usuario_nome,
        u.perfil                        AS usuario_perfil,

        -- Valores
        ISNULL(oi.valor_bruto,       0) AS valor_bruto,
        ISNULL(oi.valor_desconto,    0) AS valor_desconto_total,
        os.valor_total                  AS valor_total,
        os.valor_pago                   AS valor_pago,

        -- OS não tem forma_pagamento definida no schema atual
        NULL                            AS forma_pagamento,

        -- Itens
        oi.itens_resumo,
        ISNULL(oi.qtd_total,         0) AS itens_quantidade_total,
        ISNULL(oi.linhas_count,      0) AS itens_count,

        -- Receita / médico
        r.id                            AS receita_id,
        m.nome                          AS medico_nome,
        m.crm                           AS medico_crm,
        r.data_emissao                  AS receita_data_emissao,
        r.validade                      AS receita_validade,

        -- Datas da OS
        os.data_previsao                AS os_data_previsao,
        os.data_entrega                 AS os_data_entrega,
        os.status                       AS os_status,

        os.observacoes

    FROM ordem_servico os
    JOIN cliente c          ON c.id  = os.cliente_id
    JOIN usuario u          ON u.id  = os.usuario_id
    LEFT JOIN receita_optica r ON r.id = os.receita_id
    LEFT JOIN medico m      ON m.id  = r.medico_id
    LEFT JOIN (
        -- Agrega itens de cada OS em uma única linha
        SELECT
            op.os_id,
            SUM(op.preco_unitario * op.quantidade)  AS valor_bruto,
            SUM(op.desconto)                        AS valor_desconto,
            SUM(op.quantidade)                      AS qtd_total,
            COUNT(*)                                AS linhas_count,
            STRING_AGG(
                p.descricao
                + ' x' + CAST(op.quantidade AS VARCHAR(10))
                + ' @ R$ ' + FORMAT(op.preco_unitario, 'N2', 'pt-BR')
                + CASE WHEN op.desconto > 0
                       THEN ' (desc. R$ ' + FORMAT(op.desconto, 'N2', 'pt-BR') + ')'
                       ELSE '' END,
                ' | '
            ) WITHIN GROUP (ORDER BY p.descricao)  AS itens_resumo
        FROM os_produto op
        JOIN produto p ON p.id = op.produto_id
        GROUP BY op.os_id
    ) oi ON oi.os_id = os.id;


    -- ══════════════════════════════════════════════════════════
    -- BLOCO 3 — MOVIMENTAÇÕES DE ESTOQUE
    -- ══════════════════════════════════════════════════════════
    -- Uma linha por movimentação (já é 1 produto por registro).
    -- quantidade: positivo = entrada; negativo = saída ou ajuste para baixo.
    INSERT INTO historico_transacao (
        tipo, subtipo,
        origem_tabela, origem_id,
        data_transacao, data_registrado_em,
        usuario_id, usuario_nome, usuario_perfil,
        valor_total,
        itens_resumo, itens_quantidade_total, itens_count,
        produto_id, produto_codigo, produto_descricao,
        produto_tipo, produto_marca, produto_fornecedor,
        movimentacao_quantidade,
        observacoes
    )
    SELECT
        'MOVIMENTACAO_ESTOQUE'          AS tipo,
        me.tipo                         AS subtipo,  -- ENTRADA | SAIDA | AJUSTE

        'movimentacao_estoque'          AS origem_tabela,
        me.id                           AS origem_id,

        me.criado_em                    AS data_transacao,
        SYSDATETIME()                   AS data_registrado_em,

        -- Operador
        u.id                            AS usuario_id,
        u.nome                          AS usuario_nome,
        u.perfil                        AS usuario_perfil,

        -- Movimentações de estoque não têm valor financeiro direto
        0                               AS valor_total,

        -- Itens (resumo simplificado — apenas 1 produto por linha)
        p.descricao
            + ' x' + CAST(ABS(me.quantidade) AS VARCHAR(10))
            + ' (' + me.tipo + ')'      AS itens_resumo,
        ABS(me.quantidade)              AS itens_quantidade_total,
        1                               AS itens_count,

        -- Produto desnormalizado
        p.id                            AS produto_id,
        p.codigo                        AS produto_codigo,
        p.descricao                     AS produto_descricao,
        p.tipo                          AS produto_tipo,
        p.marca                         AS produto_marca,
        f.razao_social                  AS produto_fornecedor,

        -- Sinaliza direção: ENTRADA positivo, SAIDA/AJUSTE negativo quando reduz
        CASE me.tipo
            WHEN 'ENTRADA' THEN  ABS(me.quantidade)
            WHEN 'SAIDA'   THEN -ABS(me.quantidade)
            ELSE                 me.quantidade   -- AJUSTE mantém o sinal original
        END                             AS movimentacao_quantidade,

        me.observacao                   AS observacoes

    FROM movimentacao_estoque me
    JOIN produto p  ON p.id = me.produto_id
    JOIN usuario u  ON u.id = me.usuario_id
    JOIN fornecedor f ON f.id = p.fornecedor_id;

END;
GO
