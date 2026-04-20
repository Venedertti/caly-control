-- ============================================================
-- V7 - Conformidade com LGPD (Lei 13.709/2018)
--
-- Adiciona:
--   1. Campos de anonimização em `cliente` para atender ao Art. 18, IV
--      (direito à eliminação dos dados pessoais).
--   2. Campo `base_legal` em `receita_optica` para documentar a
--      hipótese autorizativa do Art. 11 aplicada ao dado de saúde.
--   3. Índice em audit_login.criado_em para acelerar o job de purga.
-- ============================================================

-- Cliente: marca de anonimização (direito ao esquecimento)
ALTER TABLE cliente ADD anonimizado        BIT NOT NULL CONSTRAINT DF_cliente_anonimizado DEFAULT 0;
ALTER TABLE cliente ADD data_anonimizacao  DATETIME2 NULL;

-- Receita óptica: base legal do Art. 11 da LGPD
-- Valor padrão: 'ART_11_II_C' (tutela da saúde / execução de contrato de saúde)
ALTER TABLE receita_optica ADD base_legal VARCHAR(30) NOT NULL
    CONSTRAINT DF_receita_base_legal DEFAULT 'ART_11_II_C';

-- Índice para o job de retenção de audit_login (purga registros antigos)
CREATE INDEX IX_audit_login_criado_em ON audit_login(criado_em);
