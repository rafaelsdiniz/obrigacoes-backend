CREATE TABLE obrigacao (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    titulo           VARCHAR(150)  NOT NULL,
    descricao        VARCHAR(2000) NULL,
    tipo             VARCHAR(20)   NOT NULL,
    competencia      VARCHAR(7)    NULL,
    data_vencimento  DATE          NOT NULL,
    status           VARCHAR(15)   NOT NULL,
    empresa_id       BIGINT        NOT NULL,
    responsavel_id   BIGINT        NOT NULL,
    criado_em        DATETIME(6)   NOT NULL,
    atualizado_em    DATETIME(6)   NOT NULL,
    versao           BIGINT        NOT NULL DEFAULT 0,

    CONSTRAINT pk_obrigacao PRIMARY KEY (id),
    CONSTRAINT fk_obrigacao_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_obrigacao_responsavel FOREIGN KEY (responsavel_id) REFERENCES usuario (id),
    CONSTRAINT ck_obrigacao_status CHECK (status IN ('PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDA')),
    CONSTRAINT ck_obrigacao_tipo CHECK (tipo IN ('FISCAL', 'CONTABIL', 'TRABALHISTA', 'PREVIDENCIARIA', 'SOCIETARIA', 'OUTRA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_obrigacao_vencimento ON obrigacao (data_vencimento);
CREATE INDEX idx_obrigacao_status_vencimento ON obrigacao (status, data_vencimento);

CREATE TABLE historico_status_obrigacao (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    obrigacao_id     BIGINT       NOT NULL,
    status_anterior  VARCHAR(15)  NULL,
    status_novo      VARCHAR(15)  NOT NULL,
    alterado_em      DATETIME(6)  NOT NULL,

    CONSTRAINT pk_historico_status_obrigacao PRIMARY KEY (id),
    CONSTRAINT fk_historico_status_obrigacao FOREIGN KEY (obrigacao_id) REFERENCES obrigacao (id),
    CONSTRAINT ck_historico_status_anterior CHECK (status_anterior IN ('PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDA')),
    CONSTRAINT ck_historico_status_novo CHECK (status_novo IN ('PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_historico_obrigacao_data ON historico_status_obrigacao (obrigacao_id, alterado_em);
