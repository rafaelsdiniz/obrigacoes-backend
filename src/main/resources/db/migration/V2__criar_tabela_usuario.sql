-- E-mail já é normalizado em minúsculas pela aplicação; a colação binária faz a
-- unicidade comparar exatamente o valor gravado (a padrão ignora acentos: joão = joao).
CREATE TABLE usuario (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    nome            VARCHAR(120) NOT NULL,
    email           VARCHAR(150) NOT NULL COLLATE utf8mb4_bin,
    cargo           VARCHAR(80)  NULL,
    situacao        VARCHAR(10)  NOT NULL,
    criado_em       DATETIME(6)  NOT NULL,
    atualizado_em   DATETIME(6)  NOT NULL,
    versao          BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_situacao CHECK (situacao IN ('ATIVO', 'INATIVO'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_usuario_situacao ON usuario (situacao);
CREATE INDEX idx_usuario_nome ON usuario (nome);
