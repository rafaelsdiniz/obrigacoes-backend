# ADR-0002: Flyway para versionamento do banco

**Status:** Aceito · **Data:** 25/11/2026

## Contexto

O desafio exige que a estrutura do banco seja controlada por migrations executadas automaticamente na inicialização. Flyway e Liquibase são permitidos.

## Decisão

**Flyway**, com migrations em SQL puro (`src/main/resources/db/migration/V{n}__descricao.sql`), executadas na inicialização. O Hibernate roda com `ddl-auto: validate`, apenas conferindo se as entidades correspondem ao esquema.

## Alternativas consideradas

- **Liquibase**: mais recursos (rollback, formatos XML/YAML), mas mais verboso. Os recursos extras não são necessários.
- **`ddl-auto: update`**: não é versionado, não é revisável e pode alterar o banco de forma imprevisível. Inaceitável fora de protótipos.

## Consequências

- O esquema é revisado no pull request como qualquer código, em SQL legível, com restrições, índices e colações explícitas.
- Uma migration integrada nunca é alterada: correções são feitas em uma nova versão.
- Divergência entre entidade e esquema impede a aplicação de subir, e os testes de integração detectam isso no CI.
