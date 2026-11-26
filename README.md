# Obrigações API

[![CI](https://github.com/rafaelsdiniz/obrigacoes-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/rafaelsdiniz/obrigacoes-backend/actions/workflows/ci.yml)

API REST para gerenciamento de obrigações de empresas clientes de um escritório de contabilidade.

## Documentação

| Documento | Conteúdo |
|---|---|
| [Requisitos e regras de negócio](docs/01-requisitos.md) | Requisitos funcionais, regras de negócio, validações e requisitos não funcionais |
| [Arquitetura](docs/02-arquitetura.md) | Visão C4, camadas, modelo de domínio e de dados, implantação, CI/CD e segurança |
| [API](docs/03-api.md) | Convenções, formato de erros e contrato de cada recurso |
| [Estratégia de testes](docs/04-estrategia-de-testes.md) | Níveis de teste e cobertura das regras de negócio |
| [Guia de contribuição](docs/05-guia-de-contribuicao.md) | Fluxo de trabalho, commits, padrões de código e definição de pronto |
| [Decisões arquiteturais](docs/adr/README.md) | ADRs |

Andamento do desenvolvimento no [quadro do Trello](https://trello.com/b/lO3pOAnQ/sistema-de-gerenciamento-de-obrigacoes-player).

O aplicativo Flutter que consome esta API está em [obrigacoes-frontend](https://github.com/rafaelsdiniz/obrigacoes-frontend).

## Funcionalidades

- **Empresas** e **usuários**: cadastro, edição, consulta paginada com filtros e ativação/inativação. Cadastros inativos continuam visíveis, mas não recebem novas obrigações.
- **Obrigações**: vinculadas a uma empresa e a um responsável, com tipo, competência e vencimento. Seguem o fluxo **Pendente → Em andamento → Concluída**, sem pular nem voltar etapas; concluída não pode mais ser alterada. Cada mudança de status fica no histórico.
- **Anexos**: vários arquivos por obrigação (PDF, PNG, JPEG, XML, CSV, XLSX e DOCX, até 10 MB), com o tipo conferido pelo conteúdo do arquivo.
- **Resumo para o painel**: totais por status e de atrasadas; a listagem de obrigações também filtra por período de vencimento.

## Tecnologias

| Área | Escolha |
|---|---|
| Linguagem e framework | Java 21, Spring Boot 4 (Web MVC, Data JPA, Validation, Actuator) |
| Banco de dados | MySQL 8.4, com o esquema versionado pelo Flyway |
| Documentação da API | springdoc-openapi (Swagger UI) |
| Testes | JUnit 5, Mockito, AssertJ, Testcontainers (MySQL real), ArchUnit e JaCoCo |
| Entrega | Docker (multi-stage), Docker Compose e GitHub Actions publicando no GHCR |

## Execução rápida

Pré-requisito: Docker com Docker Compose.

```bash
docker compose up -d --build
```

| Serviço | Endereço |
|---|---|
| Aplicativo web | http://localhost:3000 |
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health check | http://localhost:8080/actuator/health |

O Compose sobe três serviços: `mysql`, `api` e `app` (o aplicativo Flutter web servido por nginx). A API só inicia com o banco saudável, e o aplicativo só inicia com a API saudável. As migrations do Flyway rodam na subida da API.

## Desenvolvimento local

Pré-requisitos: JDK 21 e Docker (usado pelos testes de integração e pelo banco de desenvolvimento).

```bash
./mvnw verify                # compila, executa todos os testes e confere a cobertura mínima
./mvnw spring-boot:test-run  # sobe a API com um MySQL em container (Testcontainers)
```

## Configuração do ambiente

Todas as variáveis têm valor padrão. Para mudar, copie o [.env.example](.env.example) para `.env`.

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/obrigacoes…` | URL JDBC do banco (no Compose aponta para o serviço `mysql`) |
| `DB_USERNAME` / `DB_PASSWORD` | `obrigacoes` | Credenciais do banco |
| `DB_POOL_SIZE` | `10` | Tamanho do pool de conexões |
| `SERVER_PORT` | `8080` | Porta HTTP da API |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:8081` | Origens autorizadas a chamar a API |
| `APP_FUSO_HORARIO` | `America/Sao_Paulo` | Fuso do escritório, usado para definir "hoje" no cálculo de atraso |
| `APP_ANEXOS_DIRETORIO` | `storage/anexos` | Diretório onde os anexos são gravados. No Compose é `/app/storage`, no volume `anexos-data` |
| `APP_ANEXOS_TAMANHO_MAXIMO` | `10MB` | Tamanho máximo de cada anexo |
| `DB_NAME`, `DB_ROOT_PASSWORD`, `DB_PORT`, `API_PORT`, `APP_PORT` | — | Usadas só pelo Compose: nome do banco, senha root e portas expostas |
| `API_URL_PUBLICA` | `http://localhost:8080` | Endereço da API como o navegador o enxerga, repassado ao aplicativo web |

## Estrutura do projeto

O código é organizado por funcionalidade. Cada pacote reúne tudo de um assunto do negócio: controller, service, entidade, repositório e DTOs.

```text
src/main/java/br/com/obrigacoes/
├── empresa/        cadastro de empresas e consultas com filtros
├── usuario/        cadastro de usuários (colaboradores do escritório)
├── obrigacao/      obrigações, fluxo de status, histórico e resumo
├── anexo/          envio, validação pelo conteúdo, armazenamento e download de arquivos
├── shared/         base das entidades, erros (Problem Details), validações (CNPJ) e paginação
└── config/         CORS, relógio com fuso do escritório, OpenAPI e limites de upload
src/main/resources/
├── application.yml
└── db/migration/   scripts do Flyway (V1 a V4)
src/test/java/      testes unitários, de integração (Testcontainers) e de arquitetura (ArchUnit)
docs/               requisitos, arquitetura, API, testes, contribuição e ADRs
```

## Decisões arquiteturais

Cada decisão está detalhada em um [ADR](docs/adr/README.md). Em resumo:

- **Monólito modular por funcionalidade:** simples de implantar e com fronteiras claras entre os assuntos; as regras de dependência entre camadas são verificadas pelo ArchUnit.
- **Regras de negócio no domínio:** o fluxo de status é uma máquina de estados no enum `StatusObrigacao` e na entidade `Obrigacao`. A regra vale para qualquer caminho do código e é testada sem subir o Spring.
- **Erros no padrão Problem Details (RFC 7807):** toda falha tem um `codigo` estável e, quando é de validação, a lista de erros por campo. O cliente decide pelo código, não pelo texto.
- **Concorrência:** edição com bloqueio otimista (o cliente envia a `versao` que leu e recebe 409 se ela mudou); mudança de status com bloqueio pessimista da linha, porque a regra depende do status atual.
- **Anexos fora do banco:** o arquivo vai para o disco com nome UUID e o banco guarda os metadados e o SHA-256. A gravação acontece fora da transação, e o acesso passa pela interface `ArmazenamentoArquivos`, pronta para trocar por S3.
- **CNPJ alfanumérico:** a validação já segue o novo formato da Receita Federal, que passa a valer em 2026.
- **Flyway:** o esquema do banco é criado e evolui só por scripts versionados; o Hibernate apenas valida.
- **Sem autenticação nesta versão:** decisão consciente para manter o foco no escopo do desafio (ver [ADR-0009](docs/adr/0009-sem-autenticacao-na-primeira-versao.md)).

## Testes e qualidade

- Testes unitários das regras de domínio e dos services.
- Testes de integração da API com MySQL real em container (Testcontainers).
- Testes de arquitetura (ArchUnit) e cobertura mínima de 90% verificada pelo JaCoCo no `./mvnw verify`.
- O CI compila, testa e gera a imagem Docker em todo pull request; na `main`, publica a imagem no GHCR.

Detalhes em [Estratégia de testes](docs/04-estrategia-de-testes.md).

## Melhorias futuras

- Autenticação e perfis de acesso (JWT/OAuth2), com registro de **quem** fez cada mudança de status.
- Obrigações recorrentes geradas automaticamente a cada competência.
- Notificações de vencimento por e-mail ou no aplicativo.
- Armazenamento dos anexos em S3/MinIO, para escalar com várias instâncias da API.
- Calendário fiscal com sugestão de vencimentos por tipo de obrigação e feriados.
- Observabilidade: métricas (Prometheus), tracing distribuído e logs estruturados.
