package br.com.obrigacoes.anexo;

import org.springframework.core.io.InputStreamSource;

import java.io.IOException;

@FunctionalInterface
interface Assinatura {

    boolean reconhece(InputStreamSource arquivo) throws IOException;
}
