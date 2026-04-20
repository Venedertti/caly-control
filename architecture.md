# Architecture — Sistema de Gestão para Ótica (Caly Vision)

## Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Persistência | Spring Data JPA + Hibernate 6.4 |
| Banco de dados | SQL Server Express 2019+ |
| Migrations | Flyway 9.x |
| Templates | Thymeleaf 3 + Bootstrap 5.3 + Bootstrap Icons 1.11 |
| Build | Maven 3.x |
| Servidor | Tomcat embutido |
| Segurança | Spring Security 6 |
| Validação | Bean Validation (Jakarta) |
| Utilitários | Lombok |

---

## Estrutura de Pacotes

```
com.calycontrol.otica
├── config/
│   ├── SecurityConfig.java               # Filtros, roles, cabeçalhos HTTP, sessão
│   ├── LoginController.java              # GET /login com verificação de brute force
│   ├── BruteForceProtectionListener.java # Bloqueio de IP por tentativas excessivas
│   └── DataInitializer.java              # Cria usuário admin no startup se não existir
│
├── domain/
│   ├── cliente/
│   │   ├── Cliente.java                  # Campos LGPD: anonimizado, dataAnonimizacao
│   │   ├── ClienteRepository.java
│   │   ├── ClienteService.java           # anonimizar() — direito à eliminação (Art. 18, IV)
│   │   └── ClienteController.java
│   ├── medico/
│   ├── fornecedor/
│   ├── produto/
│   │   ├── Produto.java
│   │   ├── TipoProduto.java              # Enum: ARMACAO, LENTE, OCULOS_SOL, ACESSORIO, SERVICO
│   │   └── ...
│   ├── venda/
│   │   ├── Venda.java                    # Entidade principal
│   │   ├── VendaProduto.java             # Itens da venda com subtotal()
│   │   ├── FormaPagamento.java           # Enum: DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO, PIX, CONVENIO
│   │   ├── VendaForm.java                # DTO de entrada do formulário
│   │   ├── ItemVendaForm.java            # DTO de item do formulário
│   │   ├── VendaRepository.java
│   │   ├── VendaService.java             # registrar() orquestra venda + histórico na mesma transação
│   │   └── VendaController.java
│   ├── historico/
│   │   ├── HistoricoTransacao.java       # Snapshot desnormalizado de eventos financeiros/estoque
│   │   ├── HistoricoTransacaoRepository.java  # + anonimizarPorCliente (LGPD)
│   │   └── HistoricoService.java         # registrarVenda() + anonimizarCliente()
│   ├── receita/
│   │   ├── ReceitaOptica.java            # Campo baseLegal (LGPD Art. 11, II, "c")
│   │   └── ...
│   ├── audit/
│   │   ├── AuditLogin.java               # Log de tentativas de autenticação
│   │   ├── AuditLoginRepository.java     # + deleteByCriadoEmBefore (retenção)
│   │   └── AuditRetentionJob.java        # @Scheduled — purga diária de registros > 90 dias
│   ├── lgpd/
│   │   └── LgpdController.java           # GET /lgpd/clientes/{id}/meus-dados (Art. 18, V)
│   └── usuario/
│       ├── Usuario.java
│       ├── PerfilUsuario.java            # Enum: ADMIN, VENDEDOR, TECNICO
│       └── ...
│
├── shared/
│   ├── audit/
│   │   └── AuditableEntity.java          # @MappedSuperclass com criado_em, atualizado_em
│   └── exception/
│       ├── BusinessException.java
│       └── GlobalExceptionHandler.java
│
└── OticaApplication.java
```

---

## Camadas e Responsabilidades

```
[Browser / Thymeleaf]
        │
        ▼
[Controller]        ← Recebe HTTP, valida DTOs, chama Service, retorna Model+View
        │
        ▼
[Service]           ← Regras de negócio, transações (@Transactional), orquestração
        │
        ▼
[Repository]        ← Spring Data JPA, queries JPQL/nativas
        │
        ▼
[Entity / Domain]   ← Mapeamento JPA, validações de campo
        │
        ▼
[SQL Server Express]
```

**Regras de camada:**
- Controllers não acessam Repositories diretamente
- Services não conhecem `HttpServletRequest`, `Model` ou qualquer tipo web
- Lógica de negócio nunca fica em Controller nem em Entity
- DTOs separados para entrada de formulários; Entities apenas dentro do domínio

---

## Banco de Dados

### Migrations Flyway

```
src/main/resources/db/migration/
├── V1__create_schema.sql          # Todas as tabelas base (baseline em instâncias existentes)
├── V2__seed_data.sql              # Dados de teste: fornecedores, médicos, clientes, produtos
├── V3__historico_transacao.sql    # Tabela historico_transacao + procedure sp_popular_historico_transacao
├── V4__create_venda.sql           # IF NOT EXISTS — compatibilidade com bancos baselined
├── V5__seguranca_conta.sql        # Campos tentativas_falha / bloqueado_ate em usuario
├── V6__create_os_receita.sql      # Ordens de serviço e receitas ópticas
└── V7__lgpd_conformidade.sql      # LGPD: anonimizacao em cliente, base_legal em receita_optica
```

> **Nota:** `spring.flyway.baseline-on-migrate=true` com `baseline-version=1` faz com que o Flyway
> marque bancos já existentes como "na V1" sem executar o SQL da V1. A V4 usa `IF NOT EXISTS`
> para criar as tabelas `venda` e `venda_produto` nesses casos.

**Regra crítica:** nunca alterar uma migration já aplicada em produção. Sempre criar nova versão.

### Diagrama de Entidades

```
CLIENTE ──< RECEITA_OPTICA >── MEDICO
CLIENTE ──< ORDEM_SERVICO >── RECEITA_OPTICA (nullable)
CLIENTE ──< VENDA
USUARIO ──< ORDEM_SERVICO
USUARIO ──< VENDA
ORDEM_SERVICO ──< OS_PRODUTO >── PRODUTO
VENDA ──< VENDA_PRODUTO >── PRODUTO
FORNECEDOR ──< PRODUTO
PRODUTO ──< MOVIMENTACAO_ESTOQUE

HISTORICO_TRANSACAO  ← snapshot desnormalizado de VENDA | ORDEM_SERVICO | MOVIMENTACAO_ESTOQUE
```

### Tabela `historico_transacao`

Tabela de leitura desnormalizada — cada linha é autocontida, sem joins no frontend.

| Grupo | Campos |
|---|---|
| Classificação | `tipo` (VENDA/ORDEM_SERVICO/MOVIMENTACAO_ESTOQUE), `subtipo` |
| Origem | `origem_tabela`, `origem_id`, `numero_referencia` |
| Cliente | `cliente_id`, `cliente_nome`, `cliente_cpf`, `cliente_telefone`, `cliente_email` |
| Operador | `usuario_id`, `usuario_nome`, `usuario_perfil` |
| Financeiro | `valor_bruto`, `valor_desconto_total`, `valor_total`, `valor_pago` |
| Pagamento | `forma_pagamento`, `parcelas` |
| Itens | `itens_resumo` (texto pronto), `itens_quantidade_total`, `itens_count` |
| OS/Receita | `medico_nome`, `medico_crm`, `receita_validade`, `os_status`, `os_data_entrega` |
| Estoque | `produto_codigo`, `produto_descricao`, `movimentacao_quantidade` |

Populada incrementalmente pelo `HistoricoService` a cada nova venda (mesma transação).
Pode ser reconstruída integralmente via `EXEC sp_popular_historico_transacao`.

---

## Segurança

### Autenticação
- Formulário HTML com Spring Security (session-based)
- Senhas armazenadas com `BCryptPasswordEncoder(12)` — custo 12 (~4× mais lento que o padrão 10)
- Sessão expira após 30 minutos de inatividade
- `sessionFixation().newSession()` — novo ID de sessão após login (previne session fixation)
- `maximumSessions(1)` — uma sessão simultânea por usuário

### Autorização — duas camadas complementares

```
Camada 1 — URL (SecurityFilterChain):
  /usuarios/**          → hasRole('ADMIN')
  demais rotas          → authenticated()

Camada 2 — Método (anotações):
  @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")  em controllers

Camada 3 — Template (UX apenas, não é segurança):
  sec:authorize="hasRole('ADMIN')"  em elementos HTML
```

> `sec:authorize` no Thymeleaf **não** é uma medida de segurança — apenas melhora a UX
> ocultando elementos que o usuário não pode usar. A proteção real é nas camadas 1 e 2.

### Proteção contra brute force

`BruteForceProtectionListener` rastreia tentativas por IP:
- 5 falhas em 5 minutos → IP bloqueado por 15 minutos
- Reset automático após login bem-sucedido
- Contador de minutos exibido na tela de login
- Implementação em memória (single-instance); substituir por Redis para múltiplos nós

### Cabeçalhos HTTP de segurança

| Cabeçalho | Valor |
|---|---|
| `X-Frame-Options` | `DENY` — previne clickjacking |
| `X-Content-Type-Options` | `nosniff` — previne MIME sniffing |
| `Referrer-Policy` | `same-origin` |
| `Content-Security-Policy` | Restringe scripts/estilos a `'self'` e CDN autorizado |

### Recursos estáticos

`permitAll` é configurado apenas para arquivos nomeados explicitamente:
```java
.requestMatchers("/css/caly.css", "/favicon.svg").permitAll()
```
Sem curingas amplos (`/css/**`) para evitar exposição acidental de arquivos sensíveis.

### O que Spring Boot NÃO expõe via HTTP

Arquivos da raiz do projeto (`docker-compose.yml`, `.env`, `creation.sql`, `/rsc/`) **nunca**
são servidos via HTTP — Spring Boot só serve a partir de `src/main/resources/static/`.

---

## Conformidade com a LGPD (Lei 13.709/2018)

### Bases legais aplicadas

| Dado | Base legal | Artigo |
|---|---|---|
| Dados cadastrais do cliente (nome, CPF, contatos) | Execução de contrato | Art. 7º, V |
| Dados de saúde (receita óptica) | Tutela da saúde em serviço de saúde | Art. 11, II, "c" |
| Logs de autenticação | Legítimo interesse (prevenção a fraude) | Art. 7º, IX |
| Dados financeiros em histórico | Cumprimento de obrigação legal/fiscal | Art. 7º, II |

### Direitos do titular (Art. 18)

```
GET  /lgpd/clientes/{id}/meus-dados      (ADMIN) → Exporta JSON completo do titular
POST /clientes/{id}/anonimizar            (ADMIN) → Apaga dados + propaga ao histórico
```

### Anonimização em cascata

Quando um cliente solicita exclusão (Art. 18, IV):

```
ClienteService.anonimizar(id)
    ├── Limpa: cpf, telefone, email, cep, endereco, dataNascimento
    ├── Substitui nome por "Cliente Anonimizado #{id}"
    ├── Marca: anonimizado=true, dataAnonimizacao=now()
    ├── Desativa: ativo=false
    └── HistoricoService.anonimizarCliente(id)
            └── UPDATE historico_transacao SET cliente_nome='ANONIMIZADO',
                cliente_cpf=NULL, cliente_telefone=NULL, cliente_email=NULL
                WHERE cliente_id=?
                (preserva valor_total, forma_pagamento, datas para fiscal/contábil)
```

Integridade referencial de vendas e receitas é preservada — apenas os dados
pessoais são apagados. Base legal para a retenção do ID: Art. 16, II
(cumprimento de obrigação legal/fiscal).

### Retenção de logs

`AuditRetentionJob` — `@Scheduled(cron = "0 0 3 * * *")`
- Purga `audit_login` com `criado_em < now() - 90 dias`
- Justificativa: tentativas de autenticação só têm valor investigativo no curto prazo

### Aviso ao titular

Banner LGPD exibido no formulário de cadastro (`cliente/form.html`) com:
- Finalidade do tratamento
- Base legal (Art. 7º, V / Art. 11, II, "c")
- Enumeração dos direitos (Art. 18)
- Contato do encarregado (`dpo@calycontrol.com`)

---

## Identidade Visual

| Elemento | Valor |
|---|---|
| Cor primária | Crimson `#7A1212` |
| Cor dourada (accent) | `#C4883A` |
| Fundo escuro | `#1A0505` |
| Fonte | Georgia (serifada) para marca; sistema para UI |
| Favicon | SVG: círculo crimson + letra "C" + óculos branco |
| CSS | `src/main/resources/static/css/caly.css` |

---

## Configuração por Ambiente

```
src/main/resources/
├── application.properties           # Configurações comuns (sessão, Thymeleaf)
├── application-dev.properties       # Dev: SQL Server local, logs SQL, validate
└── application-prod.properties      # Prod: sem logs SQL, pool otimizado
```

---

## Build e Empacotamento

```bash
mvn clean package -DskipTests    # Gera target/otica-1.0.0.jar
```

O `.jar` inclui Tomcat embutido. Não requer servidor externo.

---

## Infraestrutura de Produção

```
[Usuário] → HTTPS:443 → [Nginx] → HTTP:8080 → [Spring Boot .jar]
                                                      │
                                              [SQL Server Express]
```

### Serviço systemd

```ini
[Unit]
Description=Caly Vision — Sistema de Gestão
After=network.target

[Service]
User=otica
ExecStart=/usr/bin/java -jar /opt/otica/otica.jar --spring.profiles.active=prod
EnvironmentFile=/opt/otica/.env
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

---

## Decisões Arquiteturais

| Decisão | Escolha | Motivo |
|---|---|---|
| Frontend | Thymeleaf (SSR) | Menor complexidade, deploy único, sem CORS, adequado ao porte |
| Autenticação | Session-based | Correto para SSR; JWT é para REST APIs/SPAs |
| Histórico | Tabela desnormalizada | Frontend lê sem joins; imutabilidade por design |
| Migrations | Flyway | Controle de versão do schema, essencial em produção |
| Banco | SQL Server Express | Sem custo de licença, familiar ao contexto |
| Deploy | .jar + systemd | Sem Docker em produção, simples de operar |
| Brute force | Listener em memória | Suficiente para instância única; Redis se escalar |
| BCrypt custo | 12 (vs padrão 10) | ~4× mais lento por hash, punitivo para ataques |
| Eliminação LGPD | Anonimização em vez de delete | Preserva integridade fiscal/contábil das vendas (Art. 16, II) |
| Retenção de audit_login | 90 dias | Prazo suficiente para detecção de fraude, mínimo necessário |
