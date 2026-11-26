# Especificação da API REST

**Sistema de Gerenciamento de Obrigações**

| | |
|---|---|
| **Versão da API** | v1 |
| **Versão do documento** | 1.0, 25/11/2026 |
| **Documentação interativa** | `http://localhost:8080/swagger-ui.html` |
| **Contrato OpenAPI** | `http://localhost:8080/v3/api-docs` |

O Swagger é gerado a partir do código e é a fonte de verdade dos detalhes de cada campo. Este documento descreve as convenções e o contrato de alto nível.

---

## 1. Convenções

| Aspecto | Convenção |
|---|---|
| Base | `/api/v1` |
| Formato | JSON em UTF-8. Upload em `multipart/form-data`. |
| Nomes de campos | `camelCase`, em português |
| Datas e horas | ISO 8601 em UTC (`2026-11-25T14:30:00.123456Z`) |
| Datas | `AAAA-MM-DD` |
| Competência | `AAAA-MM` |
| Enums | Constantes em maiúsculas (`ATIVO`, `EM_ANDAMENTO`) |
| Criação | `201 Created` com cabeçalho `Location` e o recurso no corpo |
| Edição | `PUT` com o recurso completo e o campo `versao` |
| Ações de estado | `PATCH /{id}/{acao}`, devolvendo o recurso atualizado. Ativar e inativar não têm corpo; a mudança de status recebe `{ "status": ... }` |

### 1.1 Paginação

Listagens aceitam `page` (começa em 0), `size` (padrão 20, máximo 100) e `sort` (`campo,asc|desc`, pode repetir). Ordenar por um campo não permitido retorna `400`.

```json
{
  "conteudo": [ ],
  "pagina": 0,
  "tamanho": 20,
  "totalElementos": 57,
  "totalPaginas": 3
}
```

### 1.2 Erros

Formato RFC 7807, com `Content-Type: application/problem+json`:

```json
{
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Um ou mais campos estão inválidos.",
  "codigo": "VALIDACAO",
  "timestamp": "2026-11-25T14:30:00.123Z",
  "erros": [
    { "campo": "cnpj", "mensagem": "CNPJ inválido." }
  ]
}
```

| HTTP | `codigo` | Quando |
|---|---|---|
| 400 | `VALIDACAO` | Campos inválidos (inclui `erros`) |
| 400 | `REQUISICAO_INVALIDA` | Corpo malformado, parâmetro ausente ou de tipo incorreto, ordenação não permitida |
| 400 | `ARQUIVO_INVALIDO` | Arquivo vazio ou de tipo não permitido |
| 404 | `RECURSO_NAO_ENCONTRADO` | Registro ou rota inexistente, ou arquivo de anexo indisponível no armazenamento |
| 405 | `REQUISICAO_INVALIDA` | Método HTTP não suportado pela rota |
| 409 | `CONFLITO` | Duplicidade (com `erros` indicando o campo) ou versão desatualizada (sem `erros`) |
| 413 | `ARQUIVO_INVALIDO` | Arquivo acima do tamanho máximo |
| 415 | `REQUISICAO_INVALIDA` | Tipo de conteúdo não suportado (por exemplo, upload que não é `multipart/form-data`) |
| 422 | `REGRA_NEGOCIO` | Regra de negócio violada |
| 500 | `ERRO_INTERNO` | Erro inesperado |

O cliente deve decidir o comportamento pelo `codigo`, e não pelo texto de `detail`.

---

## 2. Empresas — `/api/v1/empresas`

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| `POST` | `/` | Cadastra empresa (sempre `ATIVO`) | 201 |
| `GET` | `/` | Lista com filtros `busca` (razão social, nome fantasia ou CNPJ) e `situacao` | 200 |
| `GET` | `/{id}` | Consulta | 200 |
| `PUT` | `/{id}` | Edita dados cadastrais | 200 |
| `PATCH` | `/{id}/ativar` | Ativa (idempotente) | 200 |
| `PATCH` | `/{id}/inativar` | Inativa (idempotente) | 200 |

Ordenação: `razaoSocial` (padrão), `nomeFantasia`, `cnpj`, `situacao`, `criadoEm`, `atualizadoEm`.

**Requisição**

```json
{
  "razaoSocial": "Comércio de Alimentos Exemplo Ltda",
  "nomeFantasia": "Mercado Exemplo",
  "cnpj": "11.222.333/0001-81",
  "email": "financeiro@exemplo.com.br",
  "telefone": "(63) 3333-4444",
  "versao": 0
}
```

`versao` é obrigatório apenas no `PUT`.

**Resposta**

```json
{
  "id": 1,
  "razaoSocial": "Comércio de Alimentos Exemplo Ltda",
  "nomeFantasia": "Mercado Exemplo",
  "cnpj": "11222333000181",
  "email": "financeiro@exemplo.com.br",
  "telefone": "(63) 3333-4444",
  "situacao": "ATIVO",
  "criadoEm": "2026-11-25T14:30:00.123456Z",
  "atualizadoEm": "2026-11-25T14:30:00.123456Z",
  "versao": 0
}
```

Erros específicos: `409` para CNPJ já cadastrado, com `erros: [{"campo": "cnpj", ...}]`.

---

## 3. Usuários — `/api/v1/usuarios`

Mesmas rotas e comportamento de empresas. Filtros: `busca` (nome ou e-mail) e `situacao`. Ordenação: `nome` (padrão), `email`, `cargo`, `situacao`, `criadoEm`, `atualizadoEm`.

```json
{
  "nome": "Ana Souza",
  "email": "ana.souza@escritorio.com.br",
  "cargo": "Analista Fiscal",
  "versao": 0
}
```

Erros específicos: `409` para e-mail já cadastrado, com `erros: [{"campo": "email", ...}]`.

---

## 4. Obrigações — `/api/v1/obrigacoes`

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| `POST` | `/` | Cadastra obrigação (sempre `PENDENTE`) | 201 |
| `GET` | `/` | Lista com filtros | 200 |
| `GET` | `/resumo` | Quantidade por status e de atrasadas | 200 |
| `GET` | `/{id}` | Consulta | 200 |
| `PUT` | `/{id}` | Edita obrigação não concluída (não altera o status) | 200 |
| `PATCH` | `/{id}/status` | Avança para o próximo status do fluxo | 200 |
| `GET` | `/{id}/historico` | Transições de status, da mais antiga à mais recente | 200 |

**Filtros da listagem**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `busca` | texto | Trecho do título ou da descrição |
| `empresaId` | número | Empresa |
| `responsavelId` | número | Usuário responsável |
| `status` | enum, pode repetir | `PENDENTE`, `EM_ANDAMENTO`, `CONCLUIDA`; `?status=PENDENTE&status=EM_ANDAMENTO` combina os dois |
| `tipo` | enum | `FISCAL`, `CONTABIL`, `TRABALHISTA`, `PREVIDENCIARIA`, `SOCIETARIA`, `OUTRA` |
| `vencimentoDe` / `vencimentoAte` | data | Intervalo de vencimento (inclusivo) |
| `atrasada` | booleano | Somente atrasadas (`true`) ou somente em dia (`false`) |

Ordenação: `dataVencimento` (padrão), `titulo`, `tipo`, `competencia`, `status`, `criadoEm`, `atualizadoEm`.

`GET /resumo` aceita `empresaId` e `responsavelId`.

**Requisição de cadastro e edição**

```json
{
  "titulo": "DCTFWeb",
  "descricao": "Declaração de débitos e créditos tributários federais",
  "tipo": "PREVIDENCIARIA",
  "competencia": "2026-10",
  "dataVencimento": "2026-11-28",
  "empresaId": 1,
  "responsavelId": 3,
  "versao": 0
}
```

`competencia` e `descricao` são opcionais. `versao` é obrigatório apenas no `PUT`.

**Mudança de status**

```json
{ "status": "EM_ANDAMENTO" }
```

O status informado precisa ser exatamente o `proximoStatus` da obrigação.

**Resposta**

```json
{
  "id": 10,
  "titulo": "DCTFWeb",
  "descricao": "Declaração de débitos e créditos tributários federais",
  "tipo": "PREVIDENCIARIA",
  "competencia": "2026-10",
  "dataVencimento": "2026-11-28",
  "status": "EM_ANDAMENTO",
  "proximoStatus": "CONCLUIDA",
  "atrasada": false,
  "empresa": { "id": 1, "razaoSocial": "Comércio de Alimentos Exemplo Ltda", "cnpj": "11222333000181", "situacao": "ATIVO" },
  "responsavel": { "id": 3, "nome": "Ana Souza", "email": "ana.souza@escritorio.com.br", "situacao": "ATIVO" },
  "criadoEm": "2026-11-25T14:30:00Z",
  "atualizadoEm": "2026-11-25T15:00:00Z",
  "versao": 1
}
```

`proximoStatus` é `null` quando a obrigação está concluída.

**Histórico**

```json
[
  { "statusAnterior": null, "statusNovo": "PENDENTE", "alteradoEm": "2026-11-25T14:30:00Z" },
  { "statusAnterior": "PENDENTE", "statusNovo": "EM_ANDAMENTO", "alteradoEm": "2026-11-25T15:00:00Z" }
]
```

**Resumo**

```json
{ "pendentes": 12, "emAndamento": 5, "concluidas": 40, "atrasadas": 3 }
```

**Erros específicos**

| HTTP | Situação |
|---|---|
| 400 | Campos inválidos, enum desconhecido, data malformada ou ano fora de 2000–2100 |
| 404 | Obrigação, empresa ou responsável inexistente |
| 409 | Versão desatualizada ou bloqueio concorrente no banco |
| 422 | Empresa ou responsável inativo em nova atribuição; transição de status inválida; edição ou mudança de status de obrigação concluída |

---

## 5. Anexos — `/api/v1/obrigacoes/{obrigacaoId}/anexos`

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| `POST` | `/` | Envia um arquivo por requisição (`multipart/form-data`, campo `arquivo`) | 201 |
| `GET` | `/` | Lista os anexos da obrigação, do mais recente ao mais antigo | 200 |
| `GET` | `/{anexoId}` | Baixa o arquivo | 200 |
| `DELETE` | `/{anexoId}` | Exclui o anexo (obrigação não concluída) | 204 |

**Resposta do envio e da listagem**

```json
{
  "id": 7,
  "nomeOriginal": "recibo-dctfweb-10-2026.pdf",
  "tipo": "PDF",
  "tipoConteudo": "application/pdf",
  "tamanho": 184233,
  "hashSha256": "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
  "criadoEm": "2026-11-25T15:10:00Z"
}
```

**Download**: corpo binário com os cabeçalhos:

```http
Content-Type: application/pdf
Content-Length: 184233
Content-Disposition: attachment; filename="relatorio outubro.pdf"; filename*=UTF-8''relat%C3%B3rio%20outubro.pdf
X-Content-Type-Options: nosniff
Content-Security-Policy: sandbox
```

O parâmetro `filename` traz uma versão ASCII do nome para clientes antigos, e `filename*` traz o nome original em UTF-8.

**Erros específicos**

| HTTP | Situação |
|---|---|
| 400 | `ARQUIVO_INVALIDO`: arquivo vazio, de tipo não permitido ou com conteúdo que não corresponde à extensão |
| 400 | `REQUISICAO_INVALIDA`: campo `arquivo` ausente, mais de um arquivo no envio ou multipart malformado |
| 404 | Obrigação inexistente, anexo que não pertence à obrigação ou arquivo indisponível no armazenamento |
| 413 | Arquivo acima do tamanho máximo |
| 415 | Corpo que não é `multipart/form-data` |
| 422 | Exclusão de anexo de obrigação concluída |

---

## 6. Operação

| Rota | Descrição |
|---|---|
| `GET /actuator/health` | Saúde geral |
| `GET /actuator/health/liveness` | Processo ativo |
| `GET /actuator/health/readiness` | Pronto para receber tráfego (inclui banco) |
| `GET /actuator/info` | Informações da aplicação |
