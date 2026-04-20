# Planning — Sistema de Gestão para Ótica

## Visão Geral

Desenvolvimento iterativo em fases, priorizando o núcleo operacional antes de funcionalidades auxiliares.
Cada fase entrega valor independente e pode ser colocada em produção incremental.

---

## Fase 1 — Fundação ✅ Concluída

Objetivo: projeto rodando com autenticação e estrutura base.

- [x] Criação do projeto Spring Boot 3.x com Java 17
- [x] Configuração do `pom.xml` (dependências: Web, JPA, Security, Thymeleaf, SQL Server, Flyway, Validation, Lombok)
- [x] Configuração do `application.properties` com perfis `dev` e `prod`
- [x] Conexão com SQL Server Express via Docker validada
- [x] Primeira migration Flyway (`V1__create_schema.sql`) com todas as tabelas
- [x] Entidade `USUARIO` + `UserDetailsService` para Spring Security
- [x] `DataInitializer` para criação do usuário admin no startup
- [x] Tela de login e logout
- [x] Controle de perfis: `ADMIN`, `VENDEDOR`, `TECNICO`
- [x] Layout base Thymeleaf (navbar, footer) com Bootstrap 5
- [x] Dashboard com cards de acesso rápido e alerta de estoque baixo
- [x] `docker-compose.yml` com SQL Server Express
- [x] `GlobalExceptionHandler` com tratamento de erros centralizado

---

## Fase 2 — Cadastros Base ✅ Concluída

Objetivo: dados mestres necessários para as operações principais.

### 2.1 Clientes ✅
- [x] CRUD completo de `CLIENTE` (lista, novo, editar, detalhe, desativar)
- [x] Busca por nome, CPF e telefone
- [x] Consulta de endereço por CEP via ViaCEP (client-side JS)
- [x] Soft delete (campo `ativo`)
- [ ] Validação de CPF no backend
- [ ] Máscara de campos no frontend (CPF, telefone, CEP)

### 2.2 Médicos ✅
- [x] CRUD completo de `MEDICO`
- [x] Validação de CRM único no backend
- [ ] Busca por nome e CRM

### 2.3 Fornecedores ✅
- [x] CRUD completo de `FORNECEDOR`
- [x] Validação de CNPJ único no backend
- [ ] Busca por razão social e CNPJ

### 2.4 Produtos / Estoque ✅
- [x] CRUD completo de `PRODUTO`
- [x] Tipos de produto: `ARMACAO`, `LENTE`, `OCULOS_SOL`, `ACESSORIO`, `SERVICO`
- [x] Alerta visual de estoque abaixo do mínimo no dashboard
- [x] Link direto do alerta de estoque para edição do produto
- [x] Busca por código, descrição e marca
- [ ] Tabela `MOVIMENTACAO_ESTOQUE` — entrada, saída e ajuste de estoque

### 2.5 Usuários ✅
- [x] CRUD completo de `USUARIO` (restrito a `ADMIN`)
- [x] Senha codificada com BCrypt (custo 12)
- [x] Soft delete (desativar usuário)

### Dados de Teste ✅
- [x] `V2__seed_data.sql` com 5 fornecedores, 5 médicos, 10 clientes e 21 produtos

---

## Fase 3 — Identidade Visual ✅ Concluída

Objetivo: aplicar a identidade da marca Caly Vision Ótica em toda a interface.

- [x] Favicon SVG gerado a partir do logo da marca (crimson + óculos + letra C)
- [x] CSS de identidade visual (`caly.css`) com paleta: crimson `#7A1212`, dourado `#C4883A`, fundo escuro
- [x] Navbar reformulada com logo inline SVG, ícones Bootstrap e cores da marca
- [x] Tela de login reestilizada com gradiente crimson e card com cabeçalho colorido
- [x] Títulos de página com classe `page-title` e borda decorativa
- [x] Todos os templates atualizados com títulos "— Caly Vision Ótica"

---

## Fase 4 — Vendas ✅ Concluída

Objetivo: vendas rápidas de balcão sem OS (armações prontas, acessórios, etc.).

- [x] Entidade `VENDA` e `VENDA_PRODUTO` com domínio completo
- [x] Enum `FormaPagamento`: `DINHEIRO`, `CARTAO_CREDITO`, `CARTAO_DEBITO`, `PIX`, `CONVENIO`
- [x] Geração automática de `numero_venda` (formato `VDA-yyyyMMddHHmmss`)
- [x] Adição dinâmica de produtos via JavaScript (sem reload de página)
- [x] Preço unitário pré-preenchido ao selecionar produto, editável
- [x] Desconto por item com subtotal calculado em tempo real
- [x] Campo de parcelas exibido condicionalmente (apenas Cartão de Crédito)
- [x] Cálculo automático de `valor_total` via `recalcularTotal()`
- [x] Tela de listagem com badge de forma de pagamento
- [x] Tela de detalhe com todos os itens e total
- [x] Link "Vendas" na navbar
- [x] `V4__create_venda.sql` com `IF NOT EXISTS` para compatibilidade com banco baselined
- [x] Baixa automática de estoque ao confirmar venda (valida estoque insuficiente)
- [ ] Impressão de recibo de venda

---

## Fase 5 — Histórico de Transações ✅ Concluída

Objetivo: tabela desnormalizada imutável para visão de histórico no frontend.

- [x] Tabela `historico_transacao` — snapshot autocontido de cada evento financeiro/estoque
- [x] Stored procedure `sp_popular_historico_transacao` — full-refresh dos três tipos de evento
- [x] `HistoricoTransacao` entity + `HistoricoTransacaoRepository`
- [x] `HistoricoService.registrarVenda()` — insere linha desnormalizada na mesma transação da venda
- [x] Itens resumidos no campo `itens_resumo` (pronto para exibição, sem joins)
- [x] Índices em `data_transacao`, `tipo`, `cliente_id`, `usuario_id`, `numero_referencia`
- [ ] Tela de histórico no frontend (listagem + filtros por tipo, período, cliente)
- [ ] Suporte a OS e movimentações de estoque via `HistoricoService`

---

## Fase 6 — Segurança ✅ Concluída

Objetivo: hardening da aplicação antes do go-live.

- [x] `@PreAuthorize` em todos os controllers (autorização real no servidor)
- [x] `sec:authorize` nos templates (UX — oculta botões sem permissão)
- [x] Restrições por role no `SecurityFilterChain` (defense-in-depth)
- [x] `permitAll` afunilado para arquivos estáticos nomeados explicitamente
- [x] `BCryptPasswordEncoder(12)` — custo aumentado (~4x mais lento por tentativa)
- [x] `sessionFixation().newSession()` — novo ID de sessão após login
- [x] `maximumSessions(1)` — uma sessão simultânea por usuário
- [x] Cabeçalhos HTTP de segurança: `X-Frame-Options`, Content-Type nosniff, Referrer-Policy, CSP
- [x] `BruteForceProtectionListener` — bloqueio de IP após 5 falhas em 5 min (15 min de bloqueio)
- [x] Mensagem de bloqueio com contador de minutos na tela de login
- [ ] Rate limiting via Spring (alternativa ao listener em memória para multi-instância)
- [ ] OAuth2 / SSO com Google ou Keycloak (opcional, para ambientes corporativos)

---

## Fase 7 — Receitas e Ordens de Serviço ✅ Concluída

Objetivo: fluxo central da ótica — atendimento óptico e montagem.

### 7.1 Receitas Ópticas ✅
- [x] Entidade e CRUD completo de `RECEITA_OPTICA`
- [x] Vinculação com `CLIENTE` e `MEDICO`
- [x] Campos separados para DP monocular (`dp_od`, `dp_oe`)
- [x] Validação de validade da receita com badge "Expirada / Válida"
- [x] Listagem geral e detalhe com tabela de graus (OD/OE)
- [x] Botão "Abrir OS com esta receita" na tela de detalhe
- [x] Link "Receitas" na navbar

### 7.2 Ordens de Serviço ✅
- [x] Entidade e CRUD completo de `ORDEM_SERVICO`
- [x] Geração automática de `numero_os` (formato `OS-yyyyMM-00001`)
- [x] Fluxo de status: `ABERTA` → `EM_PRODUCAO` → `PRONTA` → `ENTREGUE` / `CANCELADA`
- [x] Vinculação opcional com `RECEITA_OPTICA`
- [x] Adição dinâmica de produtos/serviços via JS (mesmo padrão das vendas)
- [x] Cálculo automático de `valor_total` e controle de `valor_pago`
- [x] Cards do dashboard com contagem real de OS abertas e prontas
- [x] Link "OS" na navbar
- [ ] Impressão da OS (página otimizada para impressão via CSS)
- [ ] Filtros: por status, por cliente, por período
- [ ] `HistoricoService.registrarOS()` — popular histórico na criação/atualização de OS

---

## Fase 8 — Relatórios e Dashboard Completo

Objetivo: visibilidade gerencial para o dono da ótica.

### 8.1 Dashboard
- [x] Card de estoque baixo com contagem e lista de produtos
- [ ] Total de OS abertas / em produção / prontas
- [ ] Vendas do dia / mês (a partir de `historico_transacao`)
- [ ] Últimas OS e vendas

### 8.2 Histórico de Transações (tela)
- [ ] Listagem paginada de `historico_transacao`
- [ ] Filtros: tipo, período, cliente, forma de pagamento
- [ ] Drill-down para detalhe da venda ou OS original

### 8.3 Relatórios
- [ ] Vendas por período e forma de pagamento
- [ ] OS por período e status
- [ ] Produtos mais vendidos
- [ ] Movimentação de estoque por período
- [ ] Exportação para PDF (iText ou JasperReports)

---

## Fase 9 — Qualidade e Produção

- [x] `AuditableEntity` com `criado_em` e `atualizado_em`
- [x] Soft delete em todas as entidades principais
- [ ] Validação de CPF e CNPJ com algoritmo oficial
- [ ] Máscaras de campos no frontend
- [ ] Movimentação de estoque com histórico
- [ ] `HistoricoService.registrarMovimentacao()` — popular histórico em movimentações
- [ ] Testes unitários dos serviços principais (JUnit 5 + Mockito)
- [ ] Testes de integração nas camadas de repositório
- [ ] Script de backup automático do SQL Server Express
- [ ] Configuração de `logback-spring.xml` com rotação diária
- [ ] Configuração de deploy com `systemd`
- [ ] Nginx como reverse proxy com HTTPS via Let's Encrypt

---

## Backlog Futuro (pós MVP)

- Envio de SMS/WhatsApp quando OS ficar pronta
- Importação de tabelas de lentes de fornecedores (CSV)
- App mobile (PWA) para consulta de OS
- Módulo financeiro com contas a pagar/receber
- Integração com NF-e / NFC-e
- OAuth2 / SSO com Google Workspace ou Keycloak self-hosted
