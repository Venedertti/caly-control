-- ============================================================
-- V1 - Schema inicial do Sistema de Gestão para Ótica
-- ============================================================

CREATE TABLE usuario (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    nome           VARCHAR(100)  NOT NULL,
    email          VARCHAR(150)  NOT NULL UNIQUE,
    senha_hash     VARCHAR(255)  NOT NULL,
    perfil         VARCHAR(20)   NOT NULL CHECK (perfil IN ('ADMIN','VENDEDOR','TECNICO')),
    ativo          BIT           NOT NULL DEFAULT 1,
    criado_em      DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    atualizado_em  DATETIME2
);

CREATE TABLE cliente (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    nome            VARCHAR(100)  NOT NULL,
    cpf             VARCHAR(14)   UNIQUE,
    telefone        VARCHAR(20),
    email           VARCHAR(150),
    cep             VARCHAR(9),
    endereco        VARCHAR(255),
    data_nascimento DATE,
    ativo           BIT           NOT NULL DEFAULT 1,
    criado_em       DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    atualizado_em   DATETIME2
);

CREATE TABLE medico (
    id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    nome          VARCHAR(100)  NOT NULL,
    crm           VARCHAR(20)   NOT NULL UNIQUE,
    telefone      VARCHAR(20),
    ativo         BIT           NOT NULL DEFAULT 1,
    criado_em     DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    atualizado_em DATETIME2
);

CREATE TABLE fornecedor (
    id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    razao_social  VARCHAR(150)  NOT NULL,
    cnpj          VARCHAR(18)   UNIQUE,
    telefone      VARCHAR(20),
    email         VARCHAR(150),
    representante VARCHAR(100),
    ativo         BIT           NOT NULL DEFAULT 1,
    criado_em     DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    atualizado_em DATETIME2
);

CREATE TABLE produto (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    fornecedor_id  BIGINT        NOT NULL REFERENCES fornecedor(id),
    codigo         VARCHAR(50)   NOT NULL UNIQUE,
    descricao      VARCHAR(200)  NOT NULL,
    tipo           VARCHAR(20)   NOT NULL CHECK (tipo IN ('ARMACAO','LENTE','OCULOS_SOL','ACESSORIO','SERVICO')),
    marca          VARCHAR(80),
    preco_custo    DECIMAL(10,2),
    preco_venda    DECIMAL(10,2) NOT NULL,
    estoque_atual  INT           NOT NULL DEFAULT 0,
    estoque_minimo INT           NOT NULL DEFAULT 0,
    ativo          BIT           NOT NULL DEFAULT 1,
    criado_em      DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    atualizado_em  DATETIME2
);

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

CREATE TABLE os_produto (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    os_id           BIGINT        NOT NULL REFERENCES ordem_servico(id),
    produto_id      BIGINT        NOT NULL REFERENCES produto(id),
    quantidade      INT           NOT NULL DEFAULT 1,
    preco_unitario  DECIMAL(10,2) NOT NULL,
    desconto        DECIMAL(10,2) NOT NULL DEFAULT 0
);

CREATE TABLE venda (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
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

CREATE TABLE venda_produto (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    venda_id       BIGINT        NOT NULL REFERENCES venda(id),
    produto_id     BIGINT        NOT NULL REFERENCES produto(id),
    quantidade     INT           NOT NULL DEFAULT 1,
    preco_unitario DECIMAL(10,2) NOT NULL,
    desconto       DECIMAL(10,2) NOT NULL DEFAULT 0
);

CREATE TABLE movimentacao_estoque (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    produto_id   BIGINT        NOT NULL REFERENCES produto(id),
    usuario_id   BIGINT        NOT NULL REFERENCES usuario(id),
    tipo         VARCHAR(10)   NOT NULL CHECK (tipo IN ('ENTRADA','SAIDA','AJUSTE')),
    quantidade   INT           NOT NULL,
    observacao   VARCHAR(255),
    criado_em    DATETIME2     NOT NULL DEFAULT SYSDATETIME()
);

-- Usuário admin criado via DataInitializer no startup da aplicação
