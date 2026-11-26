# ADR-0001: Monólito modular organizado por funcionalidade

**Status:** Aceito · **Data:** 25/11/2026

## Contexto

O sistema tem quatro conceitos de negócio (empresa, usuário, obrigação e anexo), um único time e um único banco. É preciso uma estrutura simples de implantar, mas que mantenha as regras de cada conceito isoladas e fáceis de encontrar.

## Decisão

Uma única aplicação Spring Boot (monólito), com pacotes **por funcionalidade** (`empresa`, `usuario`, `obrigacao`, `anexo`) e, dentro de cada um, as camadas controller, service, domínio, repository e DTOs. O que é transversal fica em `shared`. Classes internas do módulo têm visibilidade de pacote.

## Alternativas consideradas

- **Pacotes por camada** (`controllers`, `services`, `repositories`): espalha cada funcionalidade por vários diretórios e torna tudo público.
- **Microsserviços**: custo operacional (rede, consistência distribuída, várias implantações) sem benefício para o tamanho do problema.
- **Arquitetura hexagonal completa**: portas e adaptadores para tudo acrescentam indireção sem ganho real aqui. A ideia é aplicada só onde há troca provável de implementação (armazenamento de anexos, ver ADR-0007).

## Consequências

- Cada funcionalidade é coesa e pode ser lida, testada e evoluída isoladamente.
- A extração futura de um módulo para um serviço próprio é viável, pois as fronteiras já existem.
- As regras de dependência precisam ser vigiadas, o que é feito com testes ArchUnit.
