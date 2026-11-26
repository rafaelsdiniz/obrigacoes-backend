# ADR-0004: Fluxo de status como máquina de estados no domínio, com histórico

**Status:** Aceito · **Data:** 25/11/2026

## Contexto

Toda obrigação começa em Pendente e segue Pendente → Em andamento → Concluída, sem retorno e sem alteração depois de concluída. O desafio exige que o backend garanta essas regras. Também é útil saber quando cada transição aconteceu.

## Decisão

- O enum `StatusObrigacao` define o próximo status de cada etapa (`proximo()`), e só ele é aceito (`podeAvancarPara`).
- A entidade `Obrigacao` muda de status apenas por `alterarStatus(novoStatus, agora)`, que valida a transição e registra o histórico. Não existe *setter* de status, e o status não é aceito em cadastro ou edição.
- A API expõe `PATCH /obrigacoes/{id}/status` com o status de destino. A resposta de toda obrigação traz `proximoStatus`, para que o cliente mostre só a ação permitida sem reimplementar o fluxo.
- A mudança de status bloqueia a linha da obrigação (`SELECT … FOR UPDATE`), de modo que pedidos simultâneos são avaliados um depois do outro e o histórico nunca é duplicado.
- Não é permitido pular etapas: o fluxo descrito é sequencial, e uma obrigação concluída sem nunca ter sido iniciada não teria o início registrado no histórico.
- Cada transição, inclusive a criação, gera um registro imutável em `historico_status_obrigacao`, gravado na mesma transação.
- Uma restrição `CHECK` no banco limita os valores de status.

## Alternativas consideradas

- **Uma rota por ação (`/iniciar`, `/concluir`)**: mais explícita, mas acopla a URL a cada etapa e exige novas rotas se o fluxo ganhar estados. Com o destino no corpo e a validação no domínio, a garantia é a mesma.
- **Validação no serviço**: funcionaria, mas espalha a regra. No domínio, ela vale para qualquer caso de uso e é testável sem Spring.
- **Biblioteca de máquina de estados (Spring Statemachine)**: complexidade desproporcional para três estados.

## Consequências

- É impossível gravar uma transição inválida por qualquer caminho do código.
- A matriz completa de transições é testada com um teste parametrizado.
- O histórico permite auditoria de quando cada etapa ocorreu. O registro de **quem** fez depende de autenticação (ADR-0009).
