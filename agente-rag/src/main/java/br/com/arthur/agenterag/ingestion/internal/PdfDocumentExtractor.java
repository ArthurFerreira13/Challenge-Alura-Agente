package br.com.arthur.agenterag.ingestion.internal;


import org.springframework.stereotype.Component;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


@Component
public class PdfDocumentExtractor implements DocumentExtractor {

    @Override
    public String extract(InputStream inputStream) throws IOException {
        byte[] bytes = readAllBytes(inputStream);
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    @Override
    public boolean supports(String fileExtension) {
        return "pdf".equalsIgnoreCase(fileExtension);
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            inputStream.transferTo(buffer);
            return buffer.toByteArray();
        }
    }
}
