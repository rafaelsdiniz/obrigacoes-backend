package br.com.obrigacoes.anexo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
class ArmazenamentoLocal implements ArmazenamentoArquivos {

    private static final Pattern PASTA_VALIDA = Pattern.compile("[A-Za-z0-9_-]+");

    private static final Logger log = LoggerFactory.getLogger(ArmazenamentoLocal.class);

    private final Path raiz;

    ArmazenamentoLocal(AnexosProperties propriedades) {
        this.raiz = propriedades.diretorio().toAbsolutePath().normalize();
    }

    @Override
    public ArquivoArmazenado salvar(String pasta, InputStream conteudo) {
        if (!PASTA_VALIDA.matcher(pasta).matches()) {
            throw new IllegalArgumentException("Pasta de armazenamento inválida: " + pasta);
        }
        String chave = pasta + "/" + UUID.randomUUID();
        Path destino = resolver(chave);
        MessageDigest sha256 = novoSha256();
        try (InputStream entrada = new DigestInputStream(conteudo, sha256)) {
            Files.createDirectories(destino.getParent());
            long tamanho = Files.copy(entrada, destino);
            return new ArquivoArmazenado(chave, tamanho, HexFormat.of().formatHex(sha256.digest()));
        } catch (IOException e) {
            removerSilenciosamente(destino, e);
            throw new UncheckedIOException("Falha ao gravar o arquivo " + chave, e);
        } catch (RuntimeException e) {
            removerSilenciosamente(destino, e);
            throw e;
        }
    }

    @Override
    public Resource abrir(String chave) {
        Path arquivo = resolver(chave);
        if (!Files.isRegularFile(arquivo)) {
            log.warn("Arquivo de anexo ausente no armazenamento (chave: {})", chave);
            throw new ArquivoIndisponivelException();
        }
        return new FileSystemResource(arquivo);
    }

    @Override
    public void remover(String chave) {
        try {
            Files.deleteIfExists(resolver(chave));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao remover o arquivo " + chave, e);
        }
    }

    private Path resolver(String chave) {
        Path caminho = raiz.resolve(chave).normalize();
        if (!caminho.startsWith(raiz) || caminho.equals(raiz)) {
            throw new IllegalArgumentException("Chave de armazenamento inválida: " + chave);
        }
        return caminho;
    }

    private static void removerSilenciosamente(Path arquivo, Exception causa) {
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException falhaNaLimpeza) {
            causa.addSuppressed(falhaNaLimpeza);
        }
    }

    private static MessageDigest novoSha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM", e);
        }
    }
}
