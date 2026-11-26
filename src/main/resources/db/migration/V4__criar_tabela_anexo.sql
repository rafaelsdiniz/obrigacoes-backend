CREATE TABLE anexo (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    obrigacao_id         BIGINT        NOT NULL,
    nome_original        VARCHAR(255)  NOT NULL,
    tipo                 VARCHAR(10)   NOT NULL,
    tamanho              BIGINT        NOT NULL,
    chave_armazenamento  VARCHAR(100)  NOT NULL COLLATE utf8mb4_bin,
    hash_sha256          VARCHAR(64)   NOT NULL,
    criado_em            DATETIME(6)   NOT NULL,
    atualizado_em        DATETIME(6)   NOT NULL,
    versao               BIGINT        NOT NULL DEFAULT 0,

    CONSTRAINT pk_anexo PRIMARY KEY (id),
    CONSTRAINT fk_anexo_obrigacao FOREIGN KEY (obrigacao_id) REFERENCES obrigacao (id),
    CONSTRAINT uk_anexo_chave_armazenamento UNIQUE (chave_armazenamento),
    CONSTRAINT ck_anexo_tipo CHECK (tipo IN ('PDF', 'PNG', 'JPEG', 'XML', 'CSV', 'XLSX', 'DOCX')),
    CONSTRAINT ck_anexo_tamanho CHECK (tamanho > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_anexo_obrigacao_criado_em ON anexo (obrigacao_id, criado_em);
