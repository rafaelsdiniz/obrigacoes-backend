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

## Tecnologias

- Java 21 e Spring Boot 4
- MySQL 8.4 e Flyway
- Docker e Docker Compose
- JUnit 5, Mockito e Testcontainers
- GitHub Actions

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

## Desenvolvimento local

Pré-requisitos: JDK 21 e Docker (usado pelos testes de integração).

```bash
./mvnw verify                # compila e executa todos os testes
./mvnw spring-boot:test-run  # sobe a API com um MySQL em container
```
