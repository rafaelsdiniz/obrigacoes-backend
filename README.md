# Obrigações API

[![CI](https://github.com/rafaelsdiniz/obrigacoes-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/rafaelsdiniz/obrigacoes-backend/actions/workflows/ci.yml)

API REST para gerenciamento de obrigações de empresas clientes de um escritório de contabilidade.

> Documentação completa em construção. Acompanhe o andamento no [quadro do Trello](https://trello.com/b/lO3pOAnQ/sistema-de-gerenciamento-de-obrigacoes-player).

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
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health check | http://localhost:8080/actuator/health |

## Desenvolvimento local

Pré-requisitos: JDK 21 e Docker (usado pelos testes de integração).

```bash
./mvnw verify                # compila e executa todos os testes
./mvnw spring-boot:test-run  # sobe a API com um MySQL em container
```
