package br.com.agenterag.ingestion.controller;

import br.com.agenterag.ingestion.service.EmbeddingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vetorizacao")
public class VetorizacaoController {

    private final EmbeddingService embeddingService;

    public VetorizacaoController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping("/{documentoId}")
    public ResponseEntity<String> vetorizar(@PathVariable Long documentoId) {
        embeddingService.gerarEmbeddingsParaDocumento(documentoId);
        return ResponseEntity.ok("Processo de vetorização concluído para o documento ID: " + documentoId);
    }
}
