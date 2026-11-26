# ADR-0003: Suporte ao CNPJ alfanumérico

**Status:** Aceito · **Data:** 25/11/2026

## Contexto

A Instrução Normativa RFB nº 2.229/2024 instituiu o CNPJ alfanumérico, emitido a partir de julho de 2026: as 12 primeiras posições aceitam letras maiúsculas e dígitos, e os 2 dígitos verificadores continuam numéricos. Um escritório de contabilidade já recebe clientes com esse formato.

## Decisão

- Coluna `cnpj` como `VARCHAR(14)`, nunca numérica.
- Validação pelo módulo 11 com o valor de cada caractere igual ao seu código ASCII menos 48 (`0`–`9` valem 0–9, `A`–`Z` valem 17–42), que é compatível com o cálculo tradicional do CNPJ numérico.
- Entrada aceita com ou sem máscara. O valor é gravado sem máscara e em maiúsculas.
- Unicidade garantida no banco com colação binária.

## Consequências

- O sistema aceita os dois formatos sem distinção de regra.
- A busca por CNPJ funciona digitando com ou sem máscara.
- O frontend aplica máscara alfanumérica (`AA.AAA.AAA/AAAA-00`).
