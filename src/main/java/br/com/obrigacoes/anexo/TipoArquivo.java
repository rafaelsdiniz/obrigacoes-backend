package br.com.obrigacoes.anexo;

import org.springframework.core.io.InputStreamSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum TipoArquivo {

    PDF("application/pdf", List.of("pdf"), Assinaturas.comecaCom("%PDF-")),
    PNG("image/png", List.of("png"), Assinaturas.comecaCom(0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)),
    JPEG("image/jpeg", List.of("jpg", "jpeg"), Assinaturas.comecaCom(0xFF, 0xD8, 0xFF)),
    XML("application/xml", List.of("xml"), Assinaturas.xml()),
    CSV("text/csv", List.of("csv"), Assinaturas.texto()),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", List.of("xlsx"),
            Assinaturas.zipComEntradaEm("xl/")),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", List.of("docx"),
            Assinaturas.zipComEntradaEm("word/"));

    private final String tipoConteudo;
    private final List<String> extensoes;
    private final Assinatura assinatura;

    TipoArquivo(String tipoConteudo, List<String> extensoes, Assinatura assinatura) {
        this.tipoConteudo = tipoConteudo;
        this.extensoes = extensoes;
        this.assinatura = assinatura;
    }

    static Optional<TipoArquivo> daExtensao(String extensao) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.extensoes.contains(extensao))
                .findFirst();
    }

    static String descricaoDosPermitidos() {
        List<String> nomes = Arrays.stream(values()).map(Enum::name).toList();
        return String.join(", ", nomes.subList(0, nomes.size() - 1)) + " ou " + nomes.getLast();
    }

    boolean reconhece(InputStreamSource arquivo) throws IOException {
        return assinatura.reconhece(arquivo);
    }

    public String getTipoConteudo() {
        return tipoConteudo;
    }
}
