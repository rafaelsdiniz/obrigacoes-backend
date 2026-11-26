package br.com.obrigacoes.anexo;

import org.springframework.util.unit.DataSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class AmostrasDeArquivo {

    private AmostrasDeArquivo() {
    }

    static byte[] pdf() {
        return "%PDF-1.7\n1 0 obj << /Type /Catalog >> endobj\n%%EOF".getBytes(StandardCharsets.US_ASCII);
    }

    static byte[] png() {
        return bytes(0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 'I', 'H', 'D', 'R');
    }

    static byte[] jpeg() {
        return bytes(0xFF, 0xD8, 0xFF, 0xE0, 0x00, 0x10, 'J', 'F', 'I', 'F', 0x00);
    }

    static byte[] xml() {
        String declaracao = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<nfeProc><NFe/></nfeProc>";
        return ("\uFEFF" + declaracao).getBytes(StandardCharsets.UTF_8);
    }

    static byte[] csv() {
        return "competencia;valor\n2026-10;1.250,00\r\n".getBytes(StandardCharsets.UTF_8);
    }

    static byte[] xlsx() {
        return zip("[Content_Types].xml", "_rels/.rels", "xl/workbook.xml", "xl/worksheets/sheet1.xml");
    }

    static byte[] docx() {
        return zip("[Content_Types].xml", "_rels/.rels", "word/document.xml");
    }

    static byte[] executavel() {
        return bytes('M', 'Z', 0x90, 0x00, 0x03, 0x00, 0x00, 0x00);
    }

    static byte[] bytes(int... valores) {
        byte[] resultado = new byte[valores.length];
        for (int i = 0; i < valores.length; i++) {
            resultado[i] = (byte) valores[i];
        }
        return resultado;
    }

    static byte[] zipComPrimeiraEntradaDe(DataSize tamanho, String segundaEntrada) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(saida)) {
            zip.putNextEntry(new ZipEntry("enchimento.bin"));
            zip.write(new byte[(int) tamanho.toBytes()]);
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry(segundaEntrada));
            zip.closeEntry();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return saida.toByteArray();
    }

    static byte[] zip(String... entradas) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(saida)) {
            for (String entrada : entradas) {
                zip.putNextEntry(new ZipEntry(entrada));
                zip.write("<conteudo/>".getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return saida.toByteArray();
    }
}
