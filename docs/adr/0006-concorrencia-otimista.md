# ADR-0006: Concorrência otimista com versão enviada pelo cliente

**Status:** Aceito · **Data:** 25/11/2026

## Contexto

Dois colaboradores podem abrir o mesmo registro e salvar alterações em sequência. Sem controle, quem salvar por último apaga a alteração do outro sem que ninguém perceba.

## Decisão

- Toda entidade tem a coluna `versao`, controlada pelo `@Version` do JPA.
- As respostas incluem `versao`. O `PUT` exige a versão que o cliente leu, e a edição é recusada com `409 CONFLITO` se o registro mudou desde então.
- Conflitos detectados pelo próprio JPA na gravação também são traduzidos em `409`.
- A mudança de status é a exceção: ela não depende do que o cliente leu, só do status atual. Por isso usa bloqueio pessimista de linha durante a transação curta (ADR-0004). Falhas de bloqueio no banco também viram `409`.

## Alternativas consideradas

- **Bloqueio pessimista**: segura linhas do banco durante a interação do usuário, o que não é viável em uma API sem estado.
- **ETag e `If-Match`**: equivalente em segurança e mais idiomático em HTTP, porém menos direto de consumir no formulário. Pode ser adotado depois sem mudar o modelo.

## Consequências

- Nenhuma alteração é perdida silenciosamente.
- O frontend, ao receber `409`, oferece recarregar o registro.
