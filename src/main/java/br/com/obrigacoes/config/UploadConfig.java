package br.com.obrigacoes.config;

import br.com.obrigacoes.anexo.AnexosProperties;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class UploadConfig {

    private static final DataSize MARGEM_PARA_CABECALHOS_MULTIPART = DataSize.ofKilobytes(64);
    private static final String DIRETORIO_TEMPORARIO_DO_CONTAINER = "";
    private static final int SEMPRE_EM_DISCO = 0;

    @Bean
    MultipartConfigElement multipartConfigElement(AnexosProperties anexos) {
        long tamanhoMaximoArquivo = anexos.tamanhoMaximo().toBytes();
        return new MultipartConfigElement(DIRETORIO_TEMPORARIO_DO_CONTAINER, tamanhoMaximoArquivo,
                tamanhoMaximoArquivo + MARGEM_PARA_CABECALHOS_MULTIPART.toBytes(), SEMPRE_EM_DISCO);
    }
}
