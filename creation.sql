-- ============================================================
-- creation.sql
-- Criacao do schema do Sistema de Gestao para Otica
-- SQL Server Express 2022
-- ============================================================

USE otica_dev;

-- ------------------------------------------------------------
-- USUARIO
-- Campos: AuditableEntity (criado_em, atualizado_em)
--         + id, nome, email, senha_hash, perfil, ativo
-- PerfilUsuario: ADMIN | VENDEDOR | TECNICO
-- ------------------------------------------------------------
CREATE TABLE usuario (
    id            BIGINT        IDENTITY(1,1) PRIMARY KEY,
    nome          VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL,
    senha_hash    VARCHAR(255)  NOT NULL,
    perfil        VARCHAR(20)   NOT NULL,
    ativo         BIT           NOT NULL DEFAULT 1,
    criado_em     DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    atualizado_em DATETIME2     NULL,

    CONSTRAINT uq_usuario_email  UNIQUE (email),
    CONSTRAINT ck_usuario_perfil CHECK (perfil IN ('ADMIN', 'VENDEDOR', 'TECNICO'))
);

-- ------------------------------------------------------------
-- CLIENTE
-- Campos: AuditableEntity
--         + id, nome, cpf, telefone, email, cep, endereco,
--           data_nascimento, ativo
-- ------------------------------------------------------------
CREATE TABLE cliente (
    id              BIGINT        IDENTITY(1,1) PRIMARY KEY,
    nome            VARCHAR(100)  NOT NULL,
    cpf             VARCHAR(14)   NULL,
    telefone        VARCHAR(20)   NULL,
    email           VARCHAR(150)  NULL,
    cep             VARCHAR(9)    NULL,
    endereco        VARCHAR(255)  NULL,
    data_nascimento DATE          NULL,
    ativo           BIT           NOT NULL DEFAULT 1,
    criado_em       DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    atualizado_em   DATETIME2     NULL,

    CONSTRAINT uq_cliente_cpf UNIQUE (cpf)
);

-- ------------------------------------------------------------
-- MEDICO
-- Campos: AuditableEntity
--         + id, nome, crm, telefone, ativo
-- ------------------------------------------------------------
CREATE TABLE medico (
    id            BIGINT        IDENTITY(1,1) PRIMARY KEY,
    nome          VARCHAR(100)  NOT NULL,
    crm           VARCHAR(20)   NOT NULL,
    telefone      VARCHAR(20)   NULL,
    ativo         BIT           NOT NULL DEFAULT 1,
    criado_em     DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    atualizado_em DATETIME2     NULL,

    CONSTRAINT uq_medico_crm UNIQUE (crm)
);

-- ------------------------------------------------------------
-- FORNECEDOR
-- Campos: AuditableEntity
--         + id, razao_social, cnpj, telefone, email,
--           representante, ativo
-- ------------------------------------------------------------
CREATE TABLE fornecedor (
    id            BIGINT        IDENTITY(1,1) PRIMARY KEY,
    razao_social  VARCHAR(150)  NOT NULL,
    cnpj          VARCHAR(18)   NULL,
    telefone      VARCHAR(20)   NULL,
    email         VARCHAR(150)  NULL,
    representante VARCHAR(100)  NULL,
    ativo         BIT           NOT NULL DEFAULT 1,
    criado_em     DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    atualizado_em DATETIME2     NULL,

    CONSTRAINT uq_fornecedor_cnpj UNIQUE (cnpj)
);

-- ------------------------------------------------------------
-- PRODUTO
-- Campos: AuditableEntity
--         + id, fornecedor_id (FK), codigo, descricao, tipo,
--           marca, preco_custo, preco_venda,
--           estoque_atual, estoque_minimo, ativo
-- TipoProduto: ARMACAO | LENTE | OCULOS_SOL | ACESSORIO | SERVICO
-- ------------------------------------------------------------
CREATE TABLE produto (
    id             BIGINT         IDENTITY(1,1) PRIMARY KEY,
    fornecedor_id  BIGINT         NOT NULL,
    codigo         VARCHAR(50)    NOT NULL,
    descricao      VARCHAR(200)   NOT NULL,
    tipo           VARCHAR(20)    NOT NULL,
    marca          VARCHAR(80)    NULL,
    preco_custo    DECIMAL(10, 2) NULL,
    preco_venda    DECIMAL(10, 2) NOT NULL,
    estoque_atual  INT            NOT NULL DEFAULT 0,
    estoque_minimo INT            NOT NULL DEFAULT 0,
    ativo          BIT            NOT NULL DEFAULT 1,
    criado_em      DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    atualizado_em  DATETIME2      NULL,

    CONSTRAINT uq_produto_codigo  UNIQUE (codigo),
    CONSTRAINT ck_produto_tipo    CHECK (tipo IN ('ARMACAO', 'LENTE', 'OCULOS_SOL', 'ACESSORIO', 'SERVICO')),
    CONSTRAINT ck_produto_estoque CHECK (estoque_atual >= 0 AND estoque_minimo >= 0),
    CONSTRAINT fk_produto_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES fornecedor (id)
);

-- Usuário admin criado automaticamente via DataInitializer no startup da aplicação
