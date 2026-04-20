-- ============================================================
-- V6 - Cria tabelas ausentes em bancos baselined no V1
--      (receita_optica, ordem_servico, os_produto, movimentacao_estoque)
-- Uso de IF NOT EXISTS para ser idempotente em instalações frescas
-- ============================================================

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'receita_optica')
BEGIN
    CREATE TABLE receita_optica (
        id            BIGINT IDENTITY(1,1) PRIMARY KEY,
        cliente_id    BIGINT        NOT NULL REFERENCES cliente(id),
        medico_id     BIGINT        NOT NULL REFERENCES medico(id),
        data_emissao  DATE          NOT NULL,
        validade      DATE          NOT NULL,
        od_esf        DECIMAL(5,2),
        od_cil        DECIMAL(5,2),
        od_eixo       INT,
        oe_esf        DECIMAL(5,2),
        oe_cil        DECIMAL(5,2),
        oe_eixo       INT,
        adicao        DECIMAL(5,2),
        dp_od         DECIMAL(5,2),
        dp_oe         DECIMAL(5,2),
        observacoes   VARCHAR(500),
        criado_em     DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
        atualizado_em DATETIME2
    );
END

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'ordem_servico')
BEGIN
    CREATE TABLE ordem_servico (
        id             BIGINT IDENTITY(1,1) PRIMARY KEY,
        cliente_id     BIGINT        NOT NULL REFERENCES cliente(id),
        receita_id     BIGINT                 REFERENCES receita_optica(id),
        usuario_id     BIGINT        NOT NULL REFERENCES usuario(id),
        numero_os      VARCHAR(20)   NOT NULL UNIQUE,
        status         VARCHAR(20)   NOT NULL DEFAULT 'ABERTA'
                                     CHECK (status IN ('ABERTA','EM_PRODUCAO','PRONTA','ENTREGUE','CANCELADA')),
        valor_total    DECIMAL(10,2) NOT NULL DEFAULT 0,
        valor_pago     DECIMAL(10,2) NOT NULL DEFAULT 0,
        data_abertura  DATE          NOT NULL DEFAULT CAST(SYSDATETIME() AS DATE),
        data_previsao  DATE,
        data_entrega   DATE,
        observacoes    VARCHAR(500),
        criado_em      DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
        atualizado_em  DATETIME2
    );
END

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'os_produto')
BEGIN
    CREATE TABLE os_produto (
        id              BIGINT IDENTITY(1,1) PRIMARY KEY,
        os_id           BIGINT        NOT NULL REFERENCES ordem_servico(id),
        produto_id      BIGINT        NOT NULL REFERENCES produto(id),
        quantidade      INT           NOT NULL DEFAULT 1,
        preco_unitario  DECIMAL(10,2) NOT NULL,
        desconto        DECIMAL(10,2) NOT NULL DEFAULT 0
    );
END

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'movimentacao_estoque')
BEGIN
    CREATE TABLE movimentacao_estoque (
        id           BIGINT IDENTITY(1,1) PRIMARY KEY,
        produto_id   BIGINT        NOT NULL REFERENCES produto(id),
        usuario_id   BIGINT        NOT NULL REFERENCES usuario(id),
        tipo         VARCHAR(10)   NOT NULL CHECK (tipo IN ('ENTRADA','SAIDA','AJUSTE')),
        quantidade   INT           NOT NULL,
        observacao   VARCHAR(255),
        criado_em    DATETIME2     NOT NULL DEFAULT SYSDATETIME()
    );
END
