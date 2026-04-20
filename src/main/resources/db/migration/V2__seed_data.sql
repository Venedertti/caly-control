-- ============================================================
-- V2 - Dados de teste
-- ============================================================

-- ------------------------------------------------------------
-- FORNECEDORES
-- ------------------------------------------------------------
INSERT INTO fornecedor (razao_social, cnpj, telefone, email, representante, ativo)
VALUES
    ('Luxottica Brasil Ltda',       '12.345.678/0001-90', '(11) 3000-1000', 'vendas@luxottica.com.br',   'Carlos Mendes',   1),
    ('Indo Optical do Brasil',      '23.456.789/0001-01', '(11) 3000-2000', 'comercial@indo.com.br',     'Fernanda Lima',   1),
    ('Hoya Lens do Brasil',         '34.567.890/0001-12', '(21) 3000-3000', 'pedidos@hoya.com.br',       'Roberto Salave',  1),
    ('Essilor do Brasil',           '45.678.901/0001-23', '(11) 3000-4000', 'atendimento@essilor.com.br','Patricia Souza',  1),
    ('Charmant Group Brasil',       '56.789.012/0001-34', '(11) 3000-5000', 'vendas@charmant.com.br',    'Marcelo Farias',  1);

-- ------------------------------------------------------------
-- MEDICOS
-- ------------------------------------------------------------
INSERT INTO medico (nome, crm, telefone, ativo)
VALUES
    ('Dr. Antonio Ferreira',    'CRM/SP 12345', '(11) 98000-1001', 1),
    ('Dra. Beatriz Cavalcante', 'CRM/SP 23456', '(11) 98000-1002', 1),
    ('Dr. Carlos Eduardo Lima', 'CRM/MG 34567', '(31) 98000-1003', 1),
    ('Dra. Daniela Rocha',      'CRM/SP 45678', '(11) 98000-1004', 1),
    ('Dr. Eduardo Nascimento',  'CRM/RJ 56789', '(21) 98000-1005', 1);

-- ------------------------------------------------------------
-- CLIENTES
-- ------------------------------------------------------------
INSERT INTO cliente (nome, cpf, telefone, email, cep, endereco, data_nascimento, ativo)
VALUES
    ('Ana Paula Oliveira',   '123.456.789-00', '(11) 97001-0001', 'ana.oliveira@email.com',   '01310-100', 'Av. Paulista, 1000, Bela Vista, São Paulo - SP',      '1985-03-15', 1),
    ('Bruno Santos',         '234.567.890-11', '(11) 97001-0002', 'bruno.santos@email.com',   '01001-000', 'Pça da Sé, 1, Sé, São Paulo - SP',                     '1990-07-22', 1),
    ('Carla Mendonça',       '345.678.901-22', '(21) 97001-0003', 'carla.m@email.com',        '20040-020', 'Av. Rio Branco, 200, Centro, Rio de Janeiro - RJ',      '1978-11-30', 1),
    ('Diego Almeida',        '456.789.012-33', '(31) 97001-0004', 'diego.almeida@email.com',  '30112-010', 'Av. Afonso Pena, 500, Centro, Belo Horizonte - MG',     '1995-01-08', 1),
    ('Eduarda Ferreira',     '567.890.123-44', '(11) 97001-0005', 'eduarda.f@email.com',      '04538-133', 'Av. Brigadeiro Faria Lima, 300, Itaim Bibi, SP',        '1982-06-19', 1),
    ('Fabio Costa',          '678.901.234-55', '(11) 97001-0006', 'fabio.costa@email.com',    '05409-000', 'Rua Augusta, 900, Consolação, São Paulo - SP',          '1970-09-03', 1),
    ('Giovana Teixeira',     '789.012.345-66', '(51) 97001-0007', 'giovana.t@email.com',      '90010-280', 'Rua dos Andradas, 700, Centro Histórico, Porto Alegre', '1988-04-25', 1),
    ('Henrique Barbosa',     '890.123.456-77', '(11) 97001-0008', 'henrique.b@email.com',     '02010-000', 'Av. Tiradentes, 100, Luz, São Paulo - SP',              '1993-12-11', 1),
    ('Isabela Cardoso',      '901.234.567-88', '(85) 97001-0009', 'isabela.c@email.com',      '60135-160', 'Av. Beira Mar, 400, Meireles, Fortaleza - CE',          '1975-08-07', 1),
    ('Jorge Pereira',        '012.345.678-99', '(11) 97001-0010', 'jorge.p@email.com',        '01414-000', 'Rua Oscar Freire, 600, Jardins, São Paulo - SP',        '2000-02-14', 1);

-- ------------------------------------------------------------
-- PRODUTOS
-- ------------------------------------------------------------
INSERT INTO produto (fornecedor_id, codigo, descricao, tipo, marca, preco_custo, preco_venda, estoque_atual, estoque_minimo, ativo)
VALUES
    -- Armações
    (1, 'ARM-001', 'Armação Ray-Ban RB5154 Clubmaster Acetato Preto',  'ARMACAO', 'Ray-Ban',    120.00, 380.00,  8, 3, 1),
    (1, 'ARM-002', 'Armação Oakley OX8046 Crosslink Metal Prata',      'ARMACAO', 'Oakley',     150.00, 450.00,  5, 2, 1),
    (2, 'ARM-003', 'Armação Chilli Beans Quadrada Acetato Tartaruga',  'ARMACAO', 'Chilli Beans', 80.00, 220.00, 12, 5, 1),
    (5, 'ARM-004', 'Armação Charmant CH10571 Titanio Dourado',         'ARMACAO', 'Charmant',   200.00, 620.00,  3, 2, 1),
    (2, 'ARM-005', 'Armação Indo Trend Acetato Azul Fosco',            'ARMACAO', 'Indo',        70.00, 190.00,  2, 3, 1),  -- estoque baixo

    -- Lentes
    (3, 'LEN-001', 'Lente Hoya Sync III 1.50 Antirreflexo',           'LENTE', 'Hoya',         80.00, 280.00, 20, 5, 1),
    (3, 'LEN-002', 'Lente Hoya ID MyStyle 2 Progressive 1.67',        'LENTE', 'Hoya',        220.00, 780.00,  8, 3, 1),
    (4, 'LEN-003', 'Lente Essilor Varilux Comfort Max 1.60',          'LENTE', 'Essilor',      180.00, 650.00,  6, 3, 1),
    (4, 'LEN-004', 'Lente Essilor Crizal Forte UV 1.50',              'LENTE', 'Essilor',       90.00, 310.00, 15, 5, 1),
    (3, 'LEN-005', 'Lente Hoya BlueControl 1.50 Anti-Luz Azul',       'LENTE', 'Hoya',        100.00, 340.00,  1, 4, 1),  -- estoque baixo

    -- Óculos de Sol
    (1, 'SOL-001', 'Oculos Sol Ray-Ban RB3025 Aviador Dourado/Verde',  'OCULOS_SOL', 'Ray-Ban',  130.00, 420.00, 6, 2, 1),
    (1, 'SOL-002', 'Oculos Sol Oakley Holbrook OO9102 Preto/Cinza',    'OCULOS_SOL', 'Oakley',   160.00, 520.00, 4, 2, 1),
    (2, 'SOL-003', 'Oculos Sol Chilli Beans Redondo Acetato Rose',     'OCULOS_SOL', 'Chilli Beans', 60.00, 180.00, 10, 3, 1),
    (1, 'SOL-004', 'Oculos Sol Ray-Ban RB4165 Justin Tartaruga/Verde', 'OCULOS_SOL', 'Ray-Ban',  110.00, 360.00,  0, 2, 1),  -- estoque zero

    -- Acessórios
    (1, 'ACE-001', 'Estojo Rigido Universal Preto com Flanela',        'ACESSORIO', 'Genérico',   8.00,  25.00, 30, 10, 1),
    (1, 'ACE-002', 'Cordao para Oculos Silicone Transparente',         'ACESSORIO', 'Genérico',   3.00,  12.00, 25,  8, 1),
    (1, 'ACE-003', 'Kit Limpeza Spray + Flanela Microfibra',           'ACESSORIO', 'Genérico',   5.00,  18.00, 20,  5, 1),
    (1, 'ACE-004', 'Parafuso Reparo Kit 12 pecas Mini Chave',          'ACESSORIO', 'Genérico',   4.00,  15.00,  2,  5, 1),  -- estoque baixo

    -- Serviços
    (1, 'SRV-001', 'Ajuste e Limpeza de Armacao',                     'SERVICO', NULL,            0.00,  30.00, 0, 0, 1),
    (1, 'SRV-002', 'Troca de Plaquetas de Silicone',                  'SERVICO', NULL,            2.00,  20.00, 0, 0, 1),
    (1, 'SRV-003', 'Montagem de Lentes em Armacao',                   'SERVICO', NULL,            0.00,  50.00, 0, 0, 1);
