package br.com.agenterag.chat.controller;

import br.com.agenterag.chat.dto.ChatRequest;
import br.com.agenterag.chat.dto.ChatResponse;
import br.com.agenterag.chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> conversar(@Valid @RequestBody ChatRequest request) {
        String sessionId = (request.sessindId() != null && !request.sessindId().isBlank())
                ? request.sessindId()
                : UUID.randomUUID().toString();

        String resposta = chatService.conversar(sessionId, request.mensagem());

        return ResponseEntity.ok(new ChatResponse(resposta));
    }
}
