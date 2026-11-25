-- CNPJ e e-mail já são normalizados pela aplicação; a colação binária faz a
-- unicidade comparar exatamente o valor gravado (a padrão ignora acentos: joão = joao).
CREATE TABLE empresa (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    razao_social    VARCHAR(150) NOT NULL,
    nome_fantasia   VARCHAR(150) NULL,
    cnpj            VARCHAR(14)  NOT NULL COLLATE utf8mb4_bin,
    email           VARCHAR(150) NULL COLLATE utf8mb4_bin,
    telefone        VARCHAR(20)  NULL,
    situacao        VARCHAR(10)  NOT NULL,
    criado_em       DATETIME(6)  NOT NULL,
    atualizado_em   DATETIME(6)  NOT NULL,
    versao          BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT pk_empresa PRIMARY KEY (id),
    CONSTRAINT uk_empresa_cnpj UNIQUE (cnpj),
    CONSTRAINT ck_empresa_situacao CHECK (situacao IN ('ATIVO', 'INATIVO'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_empresa_situacao ON empresa (situacao);
CREATE INDEX idx_empresa_razao_social ON empresa (razao_social);
