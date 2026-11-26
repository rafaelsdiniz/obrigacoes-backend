package br.com.obrigacoes.shared.exception;

import org.springframework.util.unit.DataSize;

final class Tamanhos {

    private static final long BYTES_POR_KB = DataSize.ofKilobytes(1).toBytes();
    private static final long BYTES_POR_MB = DataSize.ofMegabytes(1).toBytes();

    private Tamanhos() {
    }

    static String descrever(DataSize tamanho) {
        long bytes = tamanho.toBytes();
        if (bytes > 0 && bytes % BYTES_POR_MB == 0) {
            return tamanho.toMegabytes() + " MB";
        }
        if (bytes > 0 && bytes % BYTES_POR_KB == 0) {
            return tamanho.toKilobytes() + " KB";
        }
        return bytes + " bytes";
    }
}
