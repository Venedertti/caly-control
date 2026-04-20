# Test Planning — Caly Vision Ótica

Bateria de testes manuais para validação funcional e de segurança do sistema.
Execute na ordem apresentada. Marque cada caso como ✅ OK ou ❌ FALHA.

---

## 1. Autenticação

### 1.1 Login válido
**Pré-condição:** aplicação rodando, usuário admin cadastrado.
1. Acesse `http://localhost:8080/login`
2. Digite e-mail e senha corretos
3. Clique em **Entrar**

**Esperado:** redirecionado para o dashboard, navbar exibe o e-mail do usuário.

---

### 1.2 Login com senha errada
1. Acesse `/login`
2. Digite e-mail válido com senha incorreta
3. Clique em **Entrar**

**Esperado:** permanece em `/login?erro`, mensagem "E-mail ou senha incorretos" exibida.

---

### 1.3 Brute force — bloqueio de IP
1. Tente login com senha errada **5 vezes seguidas** no mesmo navegador
2. Na 6ª tentativa, clique em **Entrar**

**Esperado:** mensagem "Muitas tentativas. Tente novamente em X minuto(s)" sem campo de senha ativo.

---

### 1.4 Brute force — bloqueio de conta
1. Tente login com o e-mail de um usuário real e senha errada **10 vezes seguidas**
   (use curl ou outro navegador para contornar o bloqueio de IP se necessário)
2. Na 11ª tentativa

**Esperado:** mensagem de conta bloqueada. Login com a senha correta também bloqueado até expirar.

---

### 1.5 Logout
1. Faça login
2. Clique em **Sair** na navbar

**Esperado:** redirecionado para `/login?saiu`, mensagem "Você saiu do sistema".

---

### 1.6 Acesso sem autenticação
1. Sem estar logado, tente acessar diretamente `http://localhost:8080/clientes`

**Esperado:** redirecionado para `/login`, nunca exibe a lista de clientes.

---

### 1.7 Sessão expirada
1. Faça login
2. Aguarde 31 minutos sem interagir
3. Tente navegar para qualquer página

**Esperado:** redirecionado para `/login?sessaoExpirada`.

---

## 2. Autorização por Perfil

### 2.1 ADMIN — acesso total
1. Faça login com usuário `ADMIN`
2. Navegue por Clientes, Médicos, Fornecedores, Produtos, Usuários, Vendas

**Esperado:** todas as seções acessíveis, todos os botões de ação visíveis.

---

### 2.2 VENDEDOR — sem acesso a Usuários
1. Faça login com usuário `VENDEDOR`
2. Na navbar: confirme que "Usuários" **não aparece**
3. Tente acessar `http://localhost:8080/usuarios` diretamente

**Esperado:** navbar não exibe "Usuários"; acesso direto via URL retorna 403.

---

### 2.3 TECNICO — sem botões de criação
1. Faça login com usuário `TECNICO`
2. Acesse `/vendas`

**Esperado:** botão "Nova venda" não aparece. Acesso a `/vendas/nova` retorna 403.

---

### 2.4 Bypass de autorização via POST direto
1. Faça login com usuário `VENDEDOR`
2. Tente `POST http://localhost:8080/usuarios` via formulário ou curl

**Esperado:** 403 Forbidden — o backend rejeita independente do frontend.

---

## 3. Cadastro de Clientes

### 3.1 Criar cliente com dados válidos
1. Login como ADMIN ou VENDEDOR
2. Vá em **Clientes → Novo cliente**
3. Preencha: Nome, CPF único, telefone, e-mail
4. Clique **Salvar**

**Esperado:** cliente aparece na lista, mensagem de sucesso exibida.

---

### 3.2 Criar cliente com CPF duplicado
1. Tente criar um segundo cliente com o mesmo CPF

**Esperado:** mensagem de erro "CPF já cadastrado para outro cliente", cliente não criado.

---

### 3.3 Busca de clientes
1. Na lista de clientes, busque por parte do nome
2. Busque por CPF parcial
3. Busque por termo inexistente

**Esperado:** resultados filtrados corretamente; busca inexistente exibe "Nenhum cliente encontrado".

---

### 3.4 Desativar cliente
1. Edite um cliente e clique em desativar (se disponível)
2. Verifique que não aparece mais na lista ativa

**Esperado:** cliente removido da listagem (soft delete, registro permanece no banco).

---

## 4. Produtos

### 4.1 Criar produto com código duplicado
1. Tente criar um produto com o mesmo código de um existente

**Esperado:** erro "Código de produto já cadastrado", produto não criado.

---

### 4.2 Alerta de estoque baixo no dashboard
1. Edite um produto e defina `estoque_atual = 1`, `estoque_minimo = 5`
2. Acesse o dashboard

**Esperado:** produto aparece no card "Estoque Baixo" e na tabela de alerta, linha clicável.

---

### 4.3 Limite de 10 linhas na tabela de estoque baixo
1. Crie ou ajuste mais de 10 produtos com estoque baixo
2. Acesse o dashboard

**Esperado:** tabela exibe no máximo 10 linhas; rodapé indica "Exibindo 10 de X produtos" com link "Ver todos".

---

## 5. Vendas

### 5.1 Registrar venda completa
1. Vá em **Vendas → Nova venda**
2. Selecione um cliente
3. Adicione 2 produtos distintos com quantidades diferentes
4. Selecione forma de pagamento **PIX**
5. Clique **Finalizar Venda**

**Esperado:** venda aparece na lista com número no formato `VDA-...`, total correto, badge PIX verde.

---

### 5.2 Venda com cartão de crédito parcelado
1. Crie nova venda, selecione **Cartão de Crédito**

**Esperado:** campo "Parcelas" aparece automaticamente. Selecione 3x e finalize.

---

### 5.3 Tentar finalizar venda sem itens
1. Preencha cliente e forma de pagamento mas **não adicione produtos**
2. Clique **Finalizar Venda**

**Esperado:** erro "Adicione pelo menos um produto" em vermelho, formulário não enviado.

---

### 5.4 Remover item antes de finalizar
1. Adicione 2 produtos na tela de nova venda
2. Clique no ícone de lixeira do primeiro item
3. Finalize

**Esperado:** venda criada com apenas 1 item, total recalculado corretamente.

---

### 5.5 Detalhe da venda
1. Na lista de vendas, clique no número de uma venda

**Esperado:** tela de detalhe mostra cliente, operador, data, itens com subtotais e total correto.

---

### 5.6 Histórico no dashboard após venda
1. Registre uma nova venda
2. Volte ao dashboard

**Esperado:** venda aparece na tabela "Últimas vendas" na primeira linha (mais recente primeiro).

---

### 5.7 Limite de 20 linhas nas últimas vendas
1. Registre mais de 20 vendas (pode usar seed ou múltiplos registros)
2. Acesse o dashboard

**Esperado:** tabela exibe no máximo 20 linhas; link "Ver todas" leva para `/vendas`.

---

## 6. Usuários (ADMIN)

### 6.1 Criar usuário com senha fraca
1. Login como ADMIN, vá em **Usuários → Novo usuário**
2. Tente senhas fracas: `123456`, `senha`, `Admin1`

**Esperado:** erro "Senha fraca. Use ao menos 8 caracteres com maiúscula, minúscula, número e símbolo."

---

### 6.2 Criar usuário com senha forte
1. Use senha: `Caly@2026`

**Esperado:** usuário criado com sucesso.

---

### 6.3 Desativar usuário
1. Desative um usuário VENDEDOR
2. Tente fazer login com esse usuário

**Esperado:** login negado, usuário não consegue acessar o sistema.

---

## 7. Segurança dos Cabeçalhos HTTP

### 7.1 Verificar cabeçalhos de resposta
1. Abra DevTools (F12) → Network
2. Faça qualquer requisição autenticada
3. Inspecione os headers da resposta

**Esperado:** presença de:
- `X-Frame-Options: DENY`
- `X-Content-Type-Options: nosniff`
- `Content-Security-Policy: default-src 'self' ...`
- `Referrer-Policy: same-origin`

---

### 7.2 Tentativa de clickjacking
1. Crie um arquivo HTML local com:
```html
<iframe src="http://localhost:8080"></iframe>
```
2. Abra no navegador

**Esperado:** iframe recusado pelo navegador devido a `X-Frame-Options: DENY`.

---

## 8. Arquivos Sensíveis

### 8.1 Acesso a arquivos da raiz do projeto
Tente acessar via navegador:
- `http://localhost:8080/.env`
- `http://localhost:8080/docker-compose.yml`
- `http://localhost:8080/rsc/`
- `http://localhost:8080/application.properties`

**Esperado:** todos retornam 302 → redirect para `/login` ou 404. Nunca o conteúdo do arquivo.

---

### 8.2 Acesso a recursos estáticos sem login
1. Sem estar logado, acesse `http://localhost:8080/css/caly.css`

**Esperado:** CSS retornado normalmente (arquivo público intencional).

---

## 9. Audit Log

### 9.1 Verificar gravação de tentativas no banco
1. Faça 3 tentativas de login com senha errada
2. Conecte ao banco e execute:
```sql
SELECT TOP 10 * FROM audit_login ORDER BY criado_em DESC;
```

**Esperado:** 3 linhas com `sucesso = 0`, `motivo_falha = 'BadCredentialsException'`, IP correto.

---

### 9.2 Verificar login bem-sucedido no audit log
1. Faça login com credenciais corretas
2. Execute a query acima

**Esperado:** linha com `sucesso = 1` para o e-mail utilizado.

---

### 9.3 Enumeração de usuários no audit log
1. Tente login com e-mail completamente inexistente: `hacker@teste.com`
2. Verifique o banco

**Esperado:** linha gravada com `motivo_falha = 'USER_NOT_FOUND'`, `usuario_id = NULL`.

---

## 10. Consistência do Histórico de Transações

### 10.1 Histórico populado após venda
1. Registre uma venda
2. Execute no banco:
```sql
SELECT * FROM historico_transacao WHERE tipo = 'VENDA' ORDER BY data_transacao DESC;
```

**Esperado:** nova linha com `cliente_nome`, `itens_resumo`, `valor_total` corretos.

---

### 10.2 Rollback em falha
1. Tente registrar uma venda com `produto_id` inexistente (manipulando o POST)
2. Verifique que nenhuma linha foi criada em `venda` nem em `historico_transacao`

**Esperado:** nenhum registro parcial — transação revertida integralmente.
