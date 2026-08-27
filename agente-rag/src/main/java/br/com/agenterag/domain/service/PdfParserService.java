package br.com.agenterag.domain.service;

import br.com.agenterag.domain.internal.Alternativa;
import br.com.agenterag.domain.internal.Questao;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfParserService {

    private static final Logger log = LoggerFactory.getLogger(PdfParserService.class);

    // Caminho do Tesseract (ajuste conforme sua instalação)
    private static final String TESSERACT_DATA_PATH = "D:/TESSERACT/tessdata";

    // Padrões para parsing (já ajustados para números no início da linha)
    private static final Pattern QUESTAO_PATTERN =
            Pattern.compile("(?m)^\\s*(\\d{1,3})\\s+");
    private static final Pattern ALTERNATIVA_PATTERN =
            Pattern.compile("(?m)^\\s*\\(?([A-D])\\)?[).]\\s+");
    private static final Pattern CABECALHO_PAGINA =
            Pattern.compile("(?imu)^\\s*\\d+º?\\s+EXAME DE ORDEM UNIFICADO.*$");
    private static final Pattern NUMERO_PAGINA_ISOLADO =
            Pattern.compile("(?m)^\\s*\\d{1,3}\\s*$");

    private static final int TAMANHO_MINIMO_TEXTO = 5000;

    // ======================== MÉTODO PRINCIPAL ========================

    public List<Questao> extrairQuestoes(byte[] pdfBytes) {
        log.info("=== INICIANDO EXTRAÇÃO DE QUESTÕES ===");
        log.info("Tamanho do PDF: {} bytes", pdfBytes.length);

        String texto = extrairTexto(pdfBytes);

        if (texto == null || texto.isBlank()) {
            log.error("❌ Nenhum texto foi extraído do PDF.");
            return List.of();
        }

        log.info("✅ Texto extraído com sucesso: {} caracteres", texto.length());

        texto = limparRuido(texto);
        return parseQuestoes(texto);
    }

    // ======================== ESTRATÉGIAS DE EXTRAÇÃO ========================

    private String extrairTexto(byte[] pdfBytes) {
        // 1. PDFBox
        log.info("📄 [1/3] Tentando extração com PDFBox...");
        String texto = extrairComPdfBox(pdfBytes);
        if (isTextoValido(texto)) {
            log.info("✅ PDFBox extraiu {} caracteres (válido).", texto.length());
            return texto;
        }
        log.warn("⚠️ PDFBox extraiu apenas {} caracteres (inválido).", texto != null ? texto.length() : 0);

        // 2. OpenPDF
        log.info("📄 [2/3] Tentando extração com OpenPDF...");
        texto = extrairComOpenPdf(pdfBytes);
        if (isTextoValido(texto)) {
            log.info("✅ OpenPDF extraiu {} caracteres (válido).", texto.length());
            return texto;
        }
        log.warn("⚠️ OpenPDF extraiu apenas {} caracteres (inválido).", texto != null ? texto.length() : 0);

        // 3. Tesseract OCR (local)
        log.info("📄 [3/3] Tentando extração com Tesseract OCR (local)...");
        texto = extrairComTesseract(pdfBytes);
        if (isTextoValido(texto)) {
            log.info("✅ Tesseract extraiu {} caracteres (válido).", texto.length());
            return texto;
        }
        log.warn("⚠️ Tesseract extraiu apenas {} caracteres (inválido).", texto != null ? texto.length() : 0);

        // 4. pdftotext (fallback)
        log.info("📄 [4/3] Tentando extração com pdftotext (Poppler)...");
        texto = extrairComPdfToText(pdfBytes);
        if (isTextoValido(texto)) {
            log.info("✅ pdftotext extraiu {} caracteres (válido).", texto.length());
            return texto;
        }
        log.warn("⚠️ pdftotext extraiu apenas {} caracteres (inválido).", texto != null ? texto.length() : 0);

        log.error("❌ Todas as estratégias de extração falharam ou extraíram texto insuficiente.");
        return null;
    }

    private boolean isTextoValido(String texto) {
        return texto != null && texto.length() >= TAMANHO_MINIMO_TEXTO;
    }

    // ---------- PDFBox ----------
    private String extrairComPdfBox(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setStartPage(3);
            stripper.setLineSeparator("\n");
            stripper.setWordSeparator(" ");

            try {
                Method method = PDFTextStripper.class.getMethod("setShouldTryToUseFontSubstitution", boolean.class);
                method.invoke(stripper, true);
            } catch (Exception e) {
                log.debug("PDFBox: setShouldTryToUseFontSubstitution não disponível.");
            }

            return stripper.getText(document);
        } catch (IOException e) {
            log.error("PDFBox erro", e);
            return null;
        }
    }

    // ---------- OpenPDF ----------
    private String extrairComOpenPdf(byte[] pdfBytes) {
        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes))) {
            StringBuilder sb = new StringBuilder();
            int totalPages = reader.getNumberOfPages();
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            for (int i = 3; i <= totalPages; i++) {
                String pageText = extractor.getTextFromPage(i);
                if (pageText != null && !pageText.isBlank()) {
                    sb.append(pageText).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("OpenPDF erro", e);
            return null;
        }
    }

    // ---------- Tesseract OCR ----------
    private String extrairComTesseract(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(TESSERACT_DATA_PATH);
            tesseract.setLanguage("por");
            tesseract.setPageSegMode(1);
            tesseract.setOcrEngineMode(1);

            StringBuilder sb = new StringBuilder();
            int totalPages = document.getNumberOfPages();
            log.info("   Tesseract: processando {} páginas a partir da página 3.", totalPages - 2);

            for (int i = 3; i <= totalPages; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i - 1, 300);
                String pageText;
                try {
                    pageText = tesseract.doOCR(image);
                } catch (TesseractException e) {
                    log.error("   Tesseract: erro na página {}", i, e);
                    pageText = "";
                }
                if (pageText != null && !pageText.isBlank()) {
                    sb.append(pageText).append("\n");
                    log.debug("   Tesseract: página {} extraiu {} caracteres.", i, pageText.length());
                } else {
                    log.debug("   Tesseract: página {} vazia.", i);
                }
            }

            return sb.toString();
        } catch (IOException e) {
            log.error("Tesseract: erro ao carregar PDF ou renderizar", e);
            return null;
        }
    }

    // ---------- pdftotext (Poppler) ----------
    private String extrairComPdfToText(byte[] pdfBytes) {
        Path tempPdf = null, tempTxt = null;
        try {
            tempPdf = Files.createTempFile("temp_", ".pdf");
            tempTxt = Files.createTempFile("out_", ".txt");
            Files.write(tempPdf, pdfBytes);

            ProcessBuilder pb = new ProcessBuilder(
                    "pdftotext", "-layout", "-enc", "UTF-8",
                    tempPdf.toString(), tempTxt.toString()
            );
            Process process = pb.start();
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return null;
            }
            if (process.exitValue() == 0) {
                return Files.readString(tempTxt);
            }
            return null;
        } catch (IOException | InterruptedException e) {
            log.debug("pdftotext não disponível.");
            return null;
        } finally {
            try { if (tempPdf != null) Files.deleteIfExists(tempPdf); } catch (IOException ignored) {}
            try { if (tempTxt != null) Files.deleteIfExists(tempTxt); } catch (IOException ignored) {}
        }
    }

    // ======================== LIMPEZA E PARSING ========================

    private String limparRuido(String texto) {
        texto = CABECALHO_PAGINA.matcher(texto).replaceAll("");
        texto = NUMERO_PAGINA_ISOLADO.matcher(texto).replaceAll("");
        return texto.replaceAll("(?m)^\\s*$\\n", "");
    }

    private List<Questao> parseQuestoes(String texto) {
        List<Questao> questoes = new ArrayList<>();
        Matcher matcher = QUESTAO_PATTERN.matcher(texto);
        List<Integer> numeros = new ArrayList<>();
        List<Integer> posicoes = new ArrayList<>();

        while (matcher.find()) {
            numeros.add(Integer.parseInt(matcher.group(1)));
            posicoes.add(matcher.start());
        }

        log.info("   Encontrados {} marcadores de questão.", numeros.size());

        for (int i = 0; i < posicoes.size(); i++) {
            int inicio = posicoes.get(i);
            int fim = (i + 1 < posicoes.size()) ? posicoes.get(i + 1) : texto.length();
            String bloco = texto.substring(inicio, fim).trim();
            Questao q = parseBloco(numeros.get(i), bloco);
            if (q != null) {
                questoes.add(q);
            }
        }
        log.info("   {} questões montadas.", questoes.size());
        return questoes;
    }

    private Questao parseBloco(int numero, String bloco) {
        Matcher altMatcher = ALTERNATIVA_PATTERN.matcher(bloco);
        List<String> letras = new ArrayList<>();
        List<Integer> inicioMatchAlt = new ArrayList<>();
        List<Integer> inicioTextoAlt = new ArrayList<>();

        while (altMatcher.find()) {
            letras.add(altMatcher.group(1));
            inicioMatchAlt.add(altMatcher.start());
            inicioTextoAlt.add(altMatcher.end());
        }

        if (letras.size() < 4) {
            log.warn("   Questão {} descartada: apenas {} alternativa(s).", numero, letras.size());
            return null;
        }

        String enunciado = bloco.substring(0, inicioMatchAlt.get(0))
                .replaceFirst("^\\s*\\d{1,3}\\s+", "")
                .trim();

        Questao questao = new Questao();
        questao.setNumeroQuestao(numero);
        questao.setEnunciado(enunciado);

        List<Alternativa> alternativas = new ArrayList<>();
        for (int i = 0; i < letras.size(); i++) {
            int inicio = inicioTextoAlt.get(i);
            int fim = (i + 1 < letras.size()) ? inicioMatchAlt.get(i + 1) : bloco.length();
            Alternativa alt = new Alternativa();
            alt.setLetra(letras.get(i));
            alt.setTexto(bloco.substring(inicio, fim).trim());
            alt.setQuestao(questao);
            alternativas.add(alt);
        }
        questao.setAlternativas(alternativas);
        return questao;
    }
}