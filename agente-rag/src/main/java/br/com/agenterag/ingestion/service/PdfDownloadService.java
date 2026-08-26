package br.com.agenterag.ingestion.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

@Service
public class PdfDownloadService {

    private final HttpClient httpClient;

    public PdfDownloadService() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    /**
     * Baixa o PDF a partir de uma URL e salva em um arquivo temporário no disco.
     */
    public Path baixarPdfParaArquivo(String urlPdf) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlPdf))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("Falha ao baixar o arquivo: HTTP " + response.statusCode());
        }

        // Cria um arquivo temporário com prefixo 'oab_ingestion_' e extensão '.pdf'
        Path tempFile = Files.createTempFile("oab_ingestion_", ".pdf");

        try (InputStream in = response.body()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return tempFile;
    }

    /**
     * Alternativa: Retorna os bytes diretamente na memória (ideal para arquivos pequenos)
     */
    public byte[] baixarPdfParaBytes(String urlPdf) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlPdf))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new IOException("Falha ao baixar o arquivo: HTTP " + response.statusCode());
        }

        return response.body();
    }
}
