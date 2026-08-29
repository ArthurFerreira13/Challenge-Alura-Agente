package br.com.agenterag.domain.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
public class PdfOcrService {

    private static final Logger log = LoggerFactory.getLogger(PdfOcrService.class);

    @Value("${tesseract.datapath:D:/TESSERACT/tessdata}")
    private String tessDataPath;

    public String extrairTexto(byte[] pdfBytes) {
        String textoNativo = extrairTextoNativo(pdfBytes);

        if (textoNativo != null && !textoNativo.isBlank() && !isCorrompidoUnicode(textoNativo)) {
            log.info("Texto extraído com sucesso via PDFBox nativo.");
            return textoNativo;
        }

        log.warn("⚠️ Mapeamento Unicode ausente ou texto ilegível. Iniciando OCR a 300 DPI via Tesseract...");
        return extrairTextoComOcr(pdfBytes);
    }

    private String extrairTextoNativo(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (Exception e) {
            log.error("Erro na extração nativa", e);
            return null;
        }
    }

    private String extrairTextoComOcr(byte[] pdfBytes) {
        StringBuilder sb = new StringBuilder();

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessDataPath);
        tesseract.setLanguage("por");

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int numPages = document.getNumberOfPages();

            for (int page = 0; page < numPages; page++) {
                log.info("Processando OCR na página {}/{}...", page + 1, numPages);
                // 300 DPI é a resolução ideal para garantir a precisão do OCR em fontes pequenas
                BufferedImage image = renderer.renderImageWithDPI(page, 300);
                String result = tesseract.doOCR(image);
                sb.append(result).append("\n");
            }
            return sb.toString();
        } catch (IOException | TesseractException e) {
            log.error("Erro na execução do OCR via Tess4J", e);
            throw new RuntimeException("Falha ao processar OCR do PDF", e);
        }
    }

    private boolean isCorrompidoUnicode(String texto) {
        if (texto.length() < 20) return true;
        // Se a proporção de caracteres especiais de substituição (\uFFFD) for maior que 2%
        long invalidos = texto.chars().filter(ch -> ch == '\uFFFD' || ch == 0).count();
        return ((double) invalidos / texto.length()) > 0.02;
    }
}