package br.com.agenterag.ingestion.service;

import br.com.agenterag.core.domain.DocumentoChunk;
import br.com.agenterag.core.repository.DocumentoChunkRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final DocumentoChunkRepository chunkRepository;
    private final EmbeddingModel embeddingModel;

    public EmbeddingService(DocumentoChunkRepository chunkRepository, EmbeddingModel embeddingModel) {
        this.chunkRepository = chunkRepository;
        this.embeddingModel = embeddingModel;
    }

    @Transactional
    public void gerarEmbeddingsParaDocumento(Long documentoId) {
        List<DocumentoChunk> chunks = chunkRepository.findByDocumentoId(documentoId);

        List<DocumentoChunk> pendentes = chunks.stream()
                .filter(c -> c.getVetorEmbedding() == null || c.getVetorEmbedding().isBlank())
                .toList();

        log.info("Iniciando geração de embeddings para {} chunks pendentes do documento ID: {}", pendentes.size(), documentoId);

        if (pendentes.isEmpty()) {
            log.info("Nenhum chunk pendente. Nada a fazer.");
            return;
        }

        List<TextSegment> segments = pendentes.stream()
                .map(c -> TextSegment.from(c.getConteudoChunk()))
                .toList();

        // O SDK do Gemini já agrupa em lotes de até 100 segmentos por requisição HTTP,
        // reduzindo drasticamente o número de chamadas comparado a um embed() por chunk.
        Response<List<Embedding>> resposta = embeddingModel.embedAll(segments);
        List<Embedding> vetores = resposta.content();

        for (int i = 0; i < pendentes.size(); i++) {
            pendentes.get(i).setVetorEmbedding(vetores.get(i).vectorAsList().toString());
        }

        chunkRepository.saveAll(pendentes);

        log.info("Embeddings gerados e salvos com sucesso! Total processados: {}", pendentes.size());
    }

}