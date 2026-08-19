package br.com.simulado.agenterag.ingestion.service;

import br.com.simulado.agenterag.core.domain.Documento;
import br.com.simulado.agenterag.core.domain.DocumentoChunk;
import br.com.simulado.agenterag.core.repository.DocumentoChunkRepository;
import br.com.simulado.agenterag.core.repository.DocumentoRepository;
import br.com.simulado.agenterag.ingestion.dto.DocumentoResponse;
import br.com.simulado.agenterag.ingestion.internal.DocumentExtractor;
import br.com.simulado.agenterag.ingestion.internal.TextChunkerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final List<DocumentExtractor> extractors;
    private final DocumentoRepository repository;
    private final DocumentoChunkRepository chunkRepository; // Injetado
    private final TextChunkerService textChunkerService;

    public IngestionService(List<DocumentExtractor> extractors,
                            DocumentoRepository repository,
                            DocumentoChunkRepository chunkRepository,
                            TextChunkerService textChunkerService) {
        this.extractors = extractors;
        this.repository = repository;
        this.chunkRepository = chunkRepository;
        this.textChunkerService = textChunkerService;
    }

    @Transactional
    public DocumentoResponse extrairTexto(MultipartFile arquivo) {
        validarArquivo(arquivo);

        String nomeArquivo = arquivo.getOriginalFilename();
        String extensao = extensaoDe(nomeArquivo);

        DocumentExtractor extractor = extractors.stream()
                .filter(e -> e.supports(extensao))
                .findFirst()
                .orElseThrow(() -> new FormatoNaoSuportadoException(extensao));

        try {
            log.info("Extraindo texto de '{}' ({} bytes) via {}",
                    nomeArquivo, arquivo.getSize(), extractor.getClass().getSimpleName());

            String texto = extractor.extract(arquivo.getInputStream());

            // 1. Salva o documento principal
            Documento documento = new Documento(
                    nomeArquivo,
                    extensao,
                    null,
                    arquivo.getBytes(),
                    texto
            );
            Documento documentoSalvo = repository.save(documento);
            log.info("Documento salvo com sucesso. ID gerado: {}", documentoSalvo.getId());

            // 2. Faz o Chunking
            List<String> pedacos = textChunkerService.dividirTexto(texto);

            // 3. Salva os chunks usando a sua entidade DocumentoChunk
            List<DocumentoChunk> chunksList = new ArrayList<>();
            for (String pedaco : pedacos) {
                chunksList.add(new DocumentoChunk(
                        documentoSalvo.getId(),
                        pedaco,
                        null // vetorEmbedding por enquanto
                ));
            }
            chunkRepository.saveAll(chunksList);
            log.info("Salvos {} chunks na tabela documento_chunk para o documento ID: {}",
                    chunksList.size(), documentoSalvo.getId());

            return DocumentoResponse.de(documentoSalvo);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao extrair texto do arquivo: " + nomeArquivo, e);
        }
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ArquivoInvalidoException("O arquivo enviado está vazio ou ausente.");
        }
    }

    private String extensaoDe(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) {
            throw new FormatoNaoSuportadoException("desconhecido");
        }
        return nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1);
    }

    public static class ArquivoInvalidoException extends RuntimeException {
        public ArquivoInvalidoException(String mensagem) { super(mensagem); }
    }

    public static class FormatoNaoSuportadoException extends RuntimeException {
        public FormatoNaoSuportadoException(String extensao) {
            super("Formato de arquivo não suportado: ." + extensao);
        }
    }
}