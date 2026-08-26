package br.com.agenterag.ingestion.service;

import br.com.agenterag.ingestion.dto.ItemProvaFgv;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class IngestionOrchestratorService {

    private final ScraperService scraperService;
    private final PdfDownloadService downloadService;

    public IngestionOrchestratorService(ScraperService scraperService, PdfDownloadService downloadService) {
        this.scraperService = scraperService;
        this.downloadService = downloadService;
    }

    public void processarSecaoFgv(String urlSecao) {
        try {
            List<ItemProvaFgv> itens = scraperService.extrairItensDaPagina(urlSecao);

            for (ItemProvaFgv item : itens) {
                // Filtra para baixar apenas os cadernos de prova ou gabaritos de interesse
                if (item.urlPdf().endsWith(".pdf")) {
                    System.out.println("Baixando: " + item.titulo());

                    // 1. Download
                    Path arquivoTemp = downloadService.baixarPdfParaArquivo(item.urlPdf());

                    // 2. Extração de texto via PDFBox 3.x
                    String textoExtraido = extrairTextoDoArquivo(arquivoTemp);

                    System.out.println("Texto extraído com sucesso! Caracteres: " + textoExtraido.length());

                    // 3. Limpeza do arquivo temporário
                    Files.deleteIfExists(arquivoTemp);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro durante o processo de ingestão: " + e.getMessage());
        }
    }

    private String extrairTextoDoArquivo(Path caminhoArquivo) throws Exception {
        try (PDDocument document = Loader.loadPDF(caminhoArquivo.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

}
