package br.com.agenterag.domain.controller;

import br.com.agenterag.domain.dto.ExameFonte;
import br.com.agenterag.domain.dto.ResultadoIngestao;
import br.com.agenterag.domain.service.BatchIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/provas")
public class BatchIngestaoController {

    private final BatchIngestionService batchIngestionService;

    public BatchIngestaoController(BatchIngestionService batchIngestionService) {
        this.batchIngestionService = batchIngestionService;
    }

    /**
     * Ingere vários exames em sequência. Cada item pode vir com URLs diretas
     * (ExameFonte.porUrlsDiretas) ou com a URL da página de seção do exame
     * na FGV pra descoberta automática via scraper (ExameFonte.porSecao).
     * A resposta HTTP só volta depois do lote inteiro processar — para
     * muitos exames isso pode demorar (delay de 2s entre cada um), então
     * ajuste o timeout do cliente (Postman) se necessário.
     */
    @PostMapping("/lote")
    public ResponseEntity<List<ResultadoIngestao>> ingerirLote(@RequestBody List<ExameFonte> exames) {
        List<ResultadoIngestao> resultados = batchIngestionService.processarLote(exames);
        return ResponseEntity.ok(resultados);
    }
}