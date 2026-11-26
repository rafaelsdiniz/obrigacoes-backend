package br.com.obrigacoes.anexo;

import org.springframework.core.io.Resource;

import java.io.InputStream;

interface ArmazenamentoArquivos {

    ArquivoArmazenado salvar(String pasta, InputStream conteudo);

    Resource abrir(String chave);

    void remover(String chave);
}
