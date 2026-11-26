package br.com.obrigacoes.anexo;

import org.springframework.core.io.InputStreamSource;
import org.springframework.util.unit.DataSize;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

final class Assinaturas {

    private static final int TAMANHO_AMOSTRA_TEXTO = 8192;
    private static final int MAXIMO_ENTRADAS_ZIP_INSPECIONADAS = 50;
    private static final long ORCAMENTO_DESCOMPACTACAO_ZIP = DataSize.ofMegabytes(16).toBytes();
    private static final byte[] BOM_UTF8 = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private Assinaturas() {
    }

    static Assinatura comecaCom(int... bytes) {
        byte[] esperado = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            esperado[i] = (byte) bytes[i];
        }
        return arquivo -> Arrays.equals(lerInicio(arquivo, esperado.length), esperado);
    }

    static Assinatura comecaCom(String prefixo) {
        return comecaCom(prefixo.chars().toArray());
    }

    static Assinatura texto() {
        return arquivo -> isTexto(lerInicio(arquivo, TAMANHO_AMOSTRA_TEXTO));
    }

    static Assinatura xml() {
        return arquivo -> {
            byte[] amostra = lerInicio(arquivo, TAMANHO_AMOSTRA_TEXTO);
            return isTexto(amostra) && primeiroCaractereRelevante(amostra) == '<';
        };
    }

    static Assinatura zipComEntradaEm(String pasta) {
        return arquivo -> {
            try (ZipInputStream zip = new ZipInputStream(arquivo.getInputStream())) {
                return contemEntradaEm(zip, pasta);
            } catch (ZipException | EOFException conteudoCorrompido) {
                return false;
            }
        };
    }

    private static boolean contemEntradaEm(ZipInputStream zip, String pasta) throws IOException {
        byte[] buffer = new byte[TAMANHO_AMOSTRA_TEXTO];
        long descompactados = 0;
        ZipEntry entrada;
        int inspecionadas = 0;
        while (inspecionadas++ < MAXIMO_ENTRADAS_ZIP_INSPECIONADAS && (entrada = zip.getNextEntry()) != null) {
            if (entrada.getName().startsWith(pasta)) {
                return true;
            }
            for (int lido; (lido = zip.read(buffer)) > 0; ) {
                descompactados += lido;
                if (descompactados > ORCAMENTO_DESCOMPACTACAO_ZIP) {
                    return false;
                }
            }
        }
        return false;
    }

    private static byte[] lerInicio(InputStreamSource arquivo, int quantidade) throws IOException {
        try (InputStream entrada = arquivo.getInputStream()) {
            return entrada.readNBytes(quantidade);
        }
    }

    private static boolean isTexto(byte[] amostra) {
        for (byte valor : amostra) {
            if (isControleBinario(valor)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isControleBinario(byte valor) {
        boolean controleAscii = valor >= 0 && valor < 0x20;
        boolean quebraOuTabulacao = valor == '\t' || valor == '\n' || valor == '\r' || valor == '\f';
        return controleAscii && !quebraOuTabulacao;
    }

    private static int primeiroCaractereRelevante(byte[] amostra) {
        int inicio = comecaComBomUtf8(amostra) ? BOM_UTF8.length : 0;
        for (int i = inicio; i < amostra.length; i++) {
            if (!Character.isWhitespace(amostra[i])) {
                return amostra[i];
            }
        }
        return -1;
    }

    private static boolean comecaComBomUtf8(byte[] amostra) {
        return amostra.length >= BOM_UTF8.length
                && Arrays.equals(amostra, 0, BOM_UTF8.length, BOM_UTF8, 0, BOM_UTF8.length);
    }
}
