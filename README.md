# Caly Vision Ótica — Sistema de Gestão

Sistema web para gestão de ótica de porte familiar. Controla clientes, receitas ópticas, ordens de serviço, estoque, vendas e histórico de transações.

**Stack:** Java 17 · Spring Boot 3.2.5 · SQL Server Express · Thymeleaf · Bootstrap 5 · Flyway

---

## Pré-requisitos

- Java 17+
- Maven 3.8+
- SQL Server Express 2019+ (porta 1433)

---

## Configuração do Ambiente de Desenvolvimento

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/caly-control.git
cd caly-control
```

### 2. Crie o banco de dados

```sql
CREATE DATABASE otica_dev;
```

### 3. Configure a senha do banco

```bash
export DB_PASSWORD=sua_senha_aqui
```

### 4. Execute

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

O Flyway criará todas as tabelas automaticamente.

Acesse: `http://localhost:8080`

**Credenciais iniciais:**
- Usuário: `admin@calycontrol.com`
- Senha: `admin123` ← altere imediatamente

---

## Módulos

| Módulo | Status | Descrição |
|---|---|---|
| Clientes | ✅ | Cadastro, busca e histórico |
| Médicos | ✅ | Cadastro de prescritores |
| Fornecedores | ✅ | Cadastro de fornecedores |
| Produtos | ✅ | Cadastro, estoque, alertas |
| Usuários | ✅ | Gestão de usuários e perfis (ADMIN) |
| Vendas | ✅ | Vendas de balcão com itens dinâmicos |
| Histórico | ✅ (backend) | Tabela desnormalizada de transações |
| Receitas Ópticas | ✅ | Receitas vinculadas a clientes e médicos |
| Ordens de Serviço | ✅ | Fluxo completo de OS |
| Relatórios | 🔜 | Gerenciais e exportação PDF |

---

## Perfis de Acesso

| Perfil | Permissões |
|---|---|
| `ADMIN` | Acesso total, incluindo usuários |
| `VENDEDOR` | Clientes, vendas, produtos, fornecedores, médicos |
| `TECNICO` | Leitura de OS e receitas, atualização de status da OS |

---

## Conformidade com a LGPD

O sistema implementa mecanismos técnicos para atender à Lei 13.709/2018:

| Item | Implementação |
|---|---|
| Base legal de cadastro (Art. 7º, V) | Execução de contrato — documentada no aviso de privacidade do formulário |
| Dados de saúde / receita óptica (Art. 11, II, "c") | Campo `base_legal` persistido em `receita_optica` (default `ART_11_II_C`) |
| Direito à eliminação (Art. 18, IV) | `POST /clientes/{id}/anonimizar` — apaga CPF, telefone, e-mail, endereço, data de nascimento e propaga a anonimização para `historico_transacao` |
| Direito à portabilidade (Art. 18, V) | `GET /lgpd/clientes/{id}/meus-dados` — exporta todos os dados do titular em JSON |
| Retenção de logs de autenticação (Art. 15/16) | Job diário `AuditRetentionJob` purga `audit_login` com mais de 90 dias |
| Aviso ao titular (Art. 9º) | Banner LGPD no formulário de cadastro com finalidade, base legal e contato do encarregado |
| Encarregado (DPO) | Contato: `dpo@calycontrol.com` (configurar no seu provedor de e-mail) |

**Operação:**
- A anonimização é irreversível e restrita a `ADMIN`
- A exportação LGPD é restrita a `ADMIN` (atendimento via encarregado)
- O log de autenticação guarda IP/User-Agent por **90 dias** para detecção de fraude

---

## Segurança

- Autenticação por formulário com sessão HTTP (session-based)
- BCrypt custo 12 para senhas
- Proteção contra brute force: bloqueio de IP após 5 tentativas falhas em 5 min (15 min de bloqueio)
- Sessão única por usuário (`maximumSessions(1)`)
- Cabeçalhos HTTP: `X-Frame-Options`, CSP, Referrer-Policy
- Autorização em duas camadas: URL (`SecurityFilterChain`) + método (`@PreAuthorize`)

---

## Migrations

```
V1__create_schema.sql          # Schema base
V2__seed_data.sql              # Dados de teste
V3__historico_transacao.sql    # Tabela de histórico + stored procedure
V4__create_venda.sql           # Compatibilidade com bancos baselined
V5__seguranca_conta.sql        # Segurança de conta (tentativas/bloqueio)
V6__create_os_receita.sql      # Ordens de serviço e receitas ópticas
V7__lgpd_conformidade.sql      # Campos de anonimização e base legal (LGPD)
```

**Regra:** nunca alterar uma migration já aplicada. Sempre criar nova versão.

---

## Testes

Os testes unitários cobrem as camadas de serviço e domínio usando **JUnit 5 + Mockito** (já incluídos pelo `spring-boot-starter-test`).

```bash
# Executar todos os testes
mvn test

# Executar testes + gerar relatório de cobertura (JaCoCo)
mvn verify

# Relatório HTML de cobertura
# Abrir: target/site/jacoco/index.html
```

### Cobertura atual

| Módulo | Classes testadas |
|---|---|
| `ClienteService` | Busca, findById, save (CPF duplicado), desativar, **anonimizar (LGPD)** |
| `ProdutoService` | Busca, findById, save (código duplicado), desativar, estoque baixo |
| `UsuarioService` | Save (e-mail duplicado, política de senha, BCrypt), desativar |
| `VendaService` | Registrar (sem itens, estoque insuficiente, sucesso), findById |
| `VendaProduto` | Cálculo de subtotal com/sem desconto |
| `Produto` | Lógica de `estoqueBaixo()` |
| `Usuario` | Lógica de `isContaBloqueada()` |
| `AuditRetentionJob` | Purga de registros de `audit_login` (LGPD Art. 15/16) |

Total: **55 testes unitários**.

---

## Qualidade de Código — SonarQube

O projeto está configurado para análise com **SonarQube local** via `sonar-maven-plugin`.

### Pré-requisito: subir o SonarQube

```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube:latest
# Aguarde ~30s e acesse http://localhost:9000
# Credenciais iniciais: admin / admin
```

### Executar análise completa

```bash
mvn verify sonar:sonar \
  -Dsonar.login=admin \
  -Dsonar.password=admin
```

### Executar apenas cobertura local (sem enviar ao Sonar)

```bash
mvn verify
# Cobertura JaCoCo em: target/site/jacoco/index.html
```

### Configuração

As propriedades estão em `sonar-project.properties` e no `pom.xml`:

| Propriedade | Valor |
|---|---|
| `sonar.projectKey` | `caly-control` |
| `sonar.projectName` | `Caly Vision Ótica` |
| `sonar.host.url` | `http://localhost:9000` |

> Para SonarCloud, adicione `sonar.organization` e aponte `sonar.host.url` para `https://sonarcloud.io`.

---

## Build

```bash
mvn clean package -DskipTests
# Artefato: target/otica-1.0.0.jar
```

---

## Variáveis de Ambiente

| Variável | Descrição | Obrigatória |
|---|---|---|
| `DB_PASSWORD` | Senha do banco SQL Server | Sim |
| `APP_PORT` | Porta da aplicação (padrão: 8080) | Não |

---

## Deploy em Produção

```bash
# 1. Empacote
mvn clean package -DskipTests

# 2. Envie para o servidor
scp target/otica-1.0.0.jar usuario@servidor:/opt/otica/otica.jar

# 3. Reinicie o serviço
ssh usuario@servidor "sudo systemctl restart otica"
```

O Flyway aplicará novas migrations automaticamente no restart.

Consulte `architecture.md` para configuração completa de Nginx, systemd e backup.
