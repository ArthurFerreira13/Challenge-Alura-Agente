package br.com.agenterag.retrieval;
import br.com.agenterag.core.domain.DocumentoChunk;
import br.com.agenterag.core.repository.DocumentoChunkRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsultaService.class);

    private final DocumentoChunkRepository chunkRepository;
    private final EmbeddingModel embeddingModel;
    private final ChatModel chatModel;

    private static final int TOP_K = 5;

    public ConsultaService(DocumentoChunkRepository chunkRepository,
                           EmbeddingModel embeddingModel,
                           ChatModel chatModel) {
        this.chunkRepository = chunkRepository;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
    }

    public ConsultaResponse responder(String pergunta) {
        // 1. Embedding da pergunta
        Embedding perguntaEmbedding = embeddingModel.embed(pergunta).content();

        // 2. Buscar todos os chunks (protótipo – substituir por pgvector depois)
        List<DocumentoChunk> todos = (List<DocumentoChunk>) chunkRepository.findAll();
        if (todos.isEmpty()) {
            return new ConsultaResponse("Nenhum documento carregado ainda.", List.of());
        }

        // 3. Calcular similaridade

        List<ChunkScore> scores = todos.stream()
                .map(chunk -> {
                    float[] vetor = parseVectorToFloat(chunk.getVetorEmbedding());
                    if (vetor == null) return null;
                    Embedding chunkEmbedding = new Embedding(vetor);
                    double similaridade = CosineSimilarity.between(perguntaEmbedding, chunkEmbedding);
                    return new ChunkScore(chunk, similaridade);
                })
                .filter(cs -> cs != null)
                .sorted(Comparator.comparingDouble(ChunkScore::score).reversed())
                .limit(TOP_K)
                .toList();

        // 4. Montar contexto
        String contexto = scores.stream()
                .map(cs -> cs.chunk().getConteudoChunk())
                .collect(Collectors.joining("\n\n---\n\n"));

        // 5. Prompt
        String prompt = """
                Você é um assistente que responde perguntas com base no contexto fornecido.
                Contexto:
                %s

                Pergunta: %s

                Responda de forma clara e direta, usando apenas as informações do contexto. Se a resposta não estiver no contexto, diga que não sabe.
                """.formatted(contexto, pergunta);

        // 6. Gerar resposta – ChatModel.chat() retorna String
        String resposta = chatModel.chat(prompt);

        // 7. Retornar
        List<Long> ids = scores.stream()
                .map(cs -> cs.chunk().getId())
                .collect(Collectors.toList());

        return new ConsultaResponse(resposta, ids);
    }

    // Converte String do vetor para float[]
    private float[] parseVectorToFloat(String vetorStr) {
        if (vetorStr == null || vetorStr.isBlank()) return null;
        try {
            String cleaned = vetorStr.replace("[", "").replace("]", "").trim();
            if (cleaned.isEmpty()) return null;
            String[] parts = cleaned.split(",");
            float[] result = new float[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Float.parseFloat(parts[i].trim());
            }
            return result;
        } catch (Exception e) {
            LOGGER.warn("Erro ao parsear vetor: {}", vetorStr, e);
            return null;
        }
    }

    private record ChunkScore(DocumentoChunk chunk, double score) {}
}
