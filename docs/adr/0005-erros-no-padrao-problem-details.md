# ADR-0005: Erros no padrão Problem Details (RFC 7807)

**Status:** Aceito · **Data:** 25/11/2026

## Contexto

O cliente precisa distinguir erros de validação, de regra de negócio, de conflito e de recurso inexistente, e exibir mensagens úteis por campo. Formatos de erro diferentes em cada endpoint dificultam o consumo.

## Decisão

- Todas as respostas de erro seguem a RFC 7807 (`ProblemDetail` do Spring), com `Content-Type: application/problem+json`.
- Campos adicionais: `codigo` (enum estável `CodigoErro`), `timestamp` e, em validações, `erros` (lista de `campo` e `mensagem`).
- Exceções de negócio estendem `ApiException`, que define o status HTTP e o código. Um único `GlobalExceptionHandler` faz a tradução.
- Erros inesperados são registrados em log e devolvidos como `500` com mensagem genérica.
- Mensagens em português, prontas para exibição ao usuário final.

## Consequências

- O frontend trata erros em um único ponto (interceptor), decidindo pelo `codigo` e exibindo `erros` junto aos campos.
- Novos erros de negócio exigem só uma nova exceção ou um novo código, sem tocar nos controllers.
- Nenhum detalhe interno (stack trace, SQL, nomes de classes) chega ao cliente.
