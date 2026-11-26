# ADR-0007: Anexos fora do banco, atrás de uma interface de armazenamento

**Status:** Aceito · **Data:** 25/11/2026

## Contexto

Cada obrigação pode ter vários documentos (PDF, imagens, XML, planilhas). É preciso armazená-los de forma segura, recuperá-los com o nome original e permitir crescer para armazenamento em nuvem.

## Decisão

- O conteúdo fica **fora do MySQL**, e o banco guarda os metadados (nome original, tipo, tamanho, chave de armazenamento, hash SHA-256).
- O acesso passa pela interface `ArmazenamentoArquivos`. A primeira implementação grava em disco local, no volume Docker `anexos-data`.
- O arquivo é gravado com uma chave UUID em `{obrigacaoId}/{uuid}`, e o nome enviado nunca é usado no disco.
- O tipo é validado pela assinatura do conteúdo (*magic bytes*), comparada com a extensão, dentro de uma lista de tipos permitidos. O tamanho máximo é configurável (padrão de 10 MB).
- Se a gravação no banco falhar depois de salvar o arquivo, o arquivo é removido, sem deixar órfãos.

## Alternativas consideradas

- **BLOB no MySQL**: simplifica o backup, mas aumenta o banco, pesa nas consultas e no pool de conexões e dificulta servir arquivos grandes.
- **S3/MinIO desde já**: adiciona um serviço à infraestrutura sem necessidade imediata. A interface permite adotá-lo depois.

## Consequências

- O banco fica pequeno e as consultas de obrigações não carregam binários.
- A troca para armazenamento em nuvem é uma nova implementação da interface, sem impacto no domínio.
- O backup precisa incluir o volume de anexos além do banco.
- Com várias instâncias da API, o disco local precisa ser compartilhado ou substituído por armazenamento em nuvem.
