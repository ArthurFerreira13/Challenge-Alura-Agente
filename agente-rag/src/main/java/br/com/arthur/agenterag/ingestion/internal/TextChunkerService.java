package br.com.arthur.agenterag.ingestion.internal;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TextChunkerService {

    private static final Logger log = LoggerFactory.getLogger(TextChunkerService.class);

    private static final int MAX_SEGMENT_SIZE_IN_CHARS = 500;
    private static final int MAX_OVERLAP_IN_CHARS = 50;

    public List<String> dividirTexto(String textoExtraido) {
        if (textoExtraido == null || textoExtraido.isBlank()) {
            return List.of();
        }

        Document document = Document.from(textoExtraido);

        DocumentSplitter splitter = DocumentSplitters.recursive(
                MAX_SEGMENT_SIZE_IN_CHARS,
                MAX_OVERLAP_IN_CHARS
        );

        List<TextSegment> segments = splitter.split(document);

        List<String> chunks = segments.stream()
                .map(TextSegment::text)
                .collect(Collectors.toList());

        log.info("Texto dividido com sucesso em {} chunks (tamanho máx: {}, overlap: {})",
                chunks.size(), MAX_SEGMENT_SIZE_IN_CHARS, MAX_OVERLAP_IN_CHARS);

        return chunks;
    }
}