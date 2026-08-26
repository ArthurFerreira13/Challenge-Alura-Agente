package br.com.agenterag.chat.service;

import br.com.agenterag.retrieval.ConsultaService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

    private final ConsultaService consultaService;
    private final Map<String, ChatMemory> sessions = new ConcurrentHashMap<>();

    public ChatService(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    public String conversar(String sessionId, String pergunta) {
        LOGGER.info("Sessão {} - Pergunta: {}", sessionId, pergunta);

        ChatMemory memory = sessions.computeIfAbsent(sessionId,
                id -> MessageWindowChatMemory.withMaxMessages(10));

        memory.add(UserMessage.from(pergunta));

        var respostaConsulta = consultaService.responder(pergunta);

        memory.add(AiMessage.from(respostaConsulta.resposta()));

        return respostaConsulta.resposta();
    }
}