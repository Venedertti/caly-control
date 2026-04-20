-- ============================================================
-- V5 - Segurança de conta
--   1. Campos de bloqueio no usuario
--   2. Tabela de auditoria de tentativas de login
-- ============================================================

-- ------------------------------------------------------------
-- 1. Campos de bloqueio na tabela usuario
--    tentativas_falha  : contador de falhas consecutivas (reset no login ok)
--    bloqueado_ate     : conta bloqueada até este instante (NULL = desbloqueada)
-- ------------------------------------------------------------
ALTER TABLE usuario
    ADD tentativas_falha  INT       NOT NULL DEFAULT 0,
        bloqueado_ate     DATETIME2     NULL;

-- ------------------------------------------------------------
-- 2. Tabela de auditoria de login
--    Registra TODAS as tentativas (sucesso e falha) com contexto
--    completo para análise forense posterior.
-- ------------------------------------------------------------
CREATE TABLE audit_login (
    id               BIGINT        IDENTITY(1,1) PRIMARY KEY,
    email_tentativa  VARCHAR(150)  NOT NULL,          -- e-mail digitado (pode não existir no sistema)
    usuario_id       BIGINT            NULL,           -- FK se o e-mail existir
    ip               VARCHAR(45)   NOT NULL,           -- IPv4 ou IPv6
    user_agent       VARCHAR(500)      NULL,
    sucesso          BIT           NOT NULL DEFAULT 0,
    motivo_falha     VARCHAR(100)      NULL,           -- BAD_CREDENTIALS | ACCOUNT_LOCKED | DISABLED | etc.
    criado_em        DATETIME2     NOT NULL DEFAULT SYSDATETIME()
);

CREATE INDEX IX_audit_login_email    ON audit_login (email_tentativa);
CREATE INDEX IX_audit_login_ip       ON audit_login (ip);
CREATE INDEX IX_audit_login_criado   ON audit_login (criado_em DESC);
