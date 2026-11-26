# ADR-0008: Arquitetura do frontend: por funcionalidade, Riverpod, go_router e Dio

**Status:** Aceito · **Data:** 25/11/2026

## Contexto

O aplicativo Flutter roda na web e no Android, consome a API REST e tem telas de listagem, formulário e detalhe para quatro funcionalidades. Ele precisa ser testável e coerente com a organização do backend.

## Decisão

- **Organização por funcionalidade** (`features/empresas`, `features/obrigacoes`…), com as camadas `data` (HTTP e repositórios), `domain` (modelos) e `presentation` (telas e estado). O que é comum fica em `core`.
- **Riverpod** para estado e injeção de dependências.
- **go_router** para rotas declarativas com URLs reais na web.
- **Dio** como cliente HTTP, com interceptor que converte Problem Details em uma exceção tipada.
- **URL da API em runtime**: na web, o nginx gera um `config.json` a partir da variável `API_URL` quando o contêiner sobe. Em desenvolvimento, usa-se `--dart-define`.

## Alternativas consideradas

- **Bloc**: robusto, porém mais verboso (eventos, estados e blocs para cada tela) para CRUDs.
- **Provider ou `setState`**: simples, mas acopla estado à árvore de widgets e dificulta os testes.
- **`http`**: mais simples que Dio, mas sem interceptores e sem progresso de upload.
- **URL fixa no build**: exigiria uma imagem por ambiente.

## Consequências

- Telas finas e testáveis, com dependências substituíveis nos testes.
- Uma única imagem Docker do frontend serve qualquer ambiente.
- Estrutura familiar para quem conhece o backend, com o mesmo corte por funcionalidade.
