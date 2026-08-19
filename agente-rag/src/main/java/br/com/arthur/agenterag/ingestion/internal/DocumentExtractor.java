package br.com.arthur.agenterag.ingestion.internal;

import java.io.IOException;
import java.io.InputStream;

/**
 * Contrato para extração de texto puro a partir de diferentes formatos de documento.
 * Cada implementação sabe extrair de um tipo de arquivo (PDF, DOCX, XLSX, PPTX, MD, CSV, JSON, HTML).
 */
public interface DocumentExtractor {

    /**
     * Extrai o conteúdo textual de um arquivo.
     */
    String extract(InputStream inputStream) throws IOException;

    /**
     * Indica se este extractor sabe processar a extensão informada (ex: "pdf", "docx").
     */
    boolean supports(String fileExtension);

}
