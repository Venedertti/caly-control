-- ============================================================
-- V4 - Tabelas venda e venda_produto
-- Usa IF NOT EXISTS para ser idempotente:
--   • Em instalações novas: V1 já criou as tabelas → bloco ignorado.
--   • Em bancos com baseline V1 (baseline-on-migrate): V1 foi pulada
--     e as tabelas podem não existir → bloco executa normalmente.
-- ============================================================

IF NOT EXISTS (
    SELECT 1 FROM sys.tables WHERE name = 'venda' AND schema_id = SCHEMA_ID('dbo')
)
BEGIN
    CREATE TABLE venda (
        id              BIGINT        IDENTITY(1,1) PRIMARY KEY,
        cliente_id      BIGINT        NOT NULL REFERENCES cliente(id),
        usuario_id      BIGINT        NOT NULL REFERENCES usuario(id),
        numero_venda    VARCHAR(20)   NOT NULL UNIQUE,
        valor_total     DECIMAL(10,2) NOT NULL DEFAULT 0,
        forma_pagamento VARCHAR(20)   NOT NULL
                        CHECK (forma_pagamento IN ('DINHEIRO','CARTAO_CREDITO','CARTAO_DEBITO','PIX','CONVENIO')),
        parcelas        INT           NOT NULL DEFAULT 1,
        data_venda      DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
        criado_em       DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
        atualizado_em   DATETIME2
    );
END;

IF NOT EXISTS (
    SELECT 1 FROM sys.tables WHERE name = 'venda_produto' AND schema_id = SCHEMA_ID('dbo')
)
BEGIN
    CREATE TABLE venda_produto (
        id             BIGINT        IDENTITY(1,1) PRIMARY KEY,
        venda_id       BIGINT        NOT NULL REFERENCES venda(id),
        produto_id     BIGINT        NOT NULL REFERENCES produto(id),
        quantidade     INT           NOT NULL DEFAULT 1,
        preco_unitario DECIMAL(10,2) NOT NULL,
        desconto       DECIMAL(10,2) NOT NULL DEFAULT 0
    );
END;
