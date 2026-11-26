# ADR-0009: Sem autenticação na primeira versão

**Status:** Aceito · **Data:** 25/11/2026

## Contexto

O desafio não pede autenticação. A entidade "usuário" representa o responsável pelas obrigações, e não uma conta de acesso. Implementar autenticação completa (cadastro de senha, recuperação, perfis) aumentaria o escopo sem cobrir nenhum requisito pedido.

## Decisão

A primeira versão não tem autenticação nem autorização. A API fica protegida apenas por CORS e pela rede onde é implantada.

## Consequências

- O escopo fica concentrado nos requisitos avaliados.
- O histórico de status registra **quando** cada transição ocorreu, mas não **quem** a executou.
- É a primeira melhoria futura: Spring Security com OAuth2/JWT, vínculo entre a conta de acesso e o `Usuario`, e registro do autor de cada transição. O modelo atual não precisa ser refeito para isso, só complementado.
