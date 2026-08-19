package br.com.simulado.agenterag.ingestion.controller;

import br.com.simulado.agenterag.ingestion.dto.DocumentoResponse;
import br.com.simulado.agenterag.ingestion.service.IngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.UncheckedIOException;
import java.util.Map;

@RestController
public class IngestionController {

    private static final Logger log = LoggerFactory.getLogger(IngestionController.class);

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/api/ingestao/extrair-texto")
    public ResponseEntity<DocumentoResponse> extrairTexto(@RequestParam("arquivo") MultipartFile arquivo) {
        DocumentoResponse resposta = ingestionService.extrairTexto(arquivo);
        log.info("Recebido arquivo: {}, tamanho: {}", arquivo.getOriginalFilename(), arquivo.getSize());

        return ResponseEntity.ok(resposta);
    }

    @ExceptionHandler(IngestionService.ArquivoInvalidoException.class)
    public ResponseEntity<Map<String, String>> tratarArquivoInvalido(IngestionService.ArquivoInvalidoException ex) {
        log.warn("Arquivo inválido recebido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(IngestionService.FormatoNaoSuportadoException.class)
    public ResponseEntity<Map<String, String>> tratarFormatoNaoSuportado(IngestionService.FormatoNaoSuportadoException ex) {
        log.warn("Formato não suportado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> tratarArquivoMuitoGrande(MaxUploadSizeExceededException ex) {
        log.warn("Upload excedeu o tamanho máximo permitido");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of("erro", "Arquivo excede o tamanho máximo permitido."));
    }

    @ExceptionHandler(UncheckedIOException.class)
    public ResponseEntity<Map<String, String>> tratarErroLeitura(UncheckedIOException ex) {
        log.error("Erro ao ler/processar arquivo", ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("erro", "Não foi possível processar o arquivo. Ele pode estar corrompido."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> tratarErroInesperado(Exception ex) {
        log.error("Erro inesperado na ingestão de documento", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("erro", "Erro interno ao processar a requisição."));
    }
}