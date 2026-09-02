package br.com.agenterag.domain.controller;

import br.com.agenterag.domain.dto.IngestaoRequest;
import br.com.agenterag.domain.dto.IngestaoResponse;
import br.com.agenterag.domain.internal.Simulado;
import br.com.agenterag.domain.service.SimuladoOrchestratorService;
import br.com.agenterag.ingestion.service.PdfDownloadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/provas")
public class ProvaIngestaoController {

    private final SimuladoOrchestratorService orchestrator;
    private final PdfDownloadService downloadService;

    public ProvaIngestaoController(SimuladoOrchestratorService orchestrator,
                                   PdfDownloadService downloadService) {
        this.orchestrator = orchestrator;
        this.downloadService = downloadService;
    }

    @PostMapping("/ingerir")
    public ResponseEntity<IngestaoResponse> ingerir(@RequestBody IngestaoRequest request) throws Exception {
        byte[] pdfProva = downloadService.baixarPdfParaBytes(request.urlProva());
        byte[] pdfGabarito = downloadService.baixarPdfParaBytes(request.urlGabarito());

        Simulado simulado = orchestrator.ingerirEGerarSimulado(
                request.edicao(), request.nomeArquivo(), pdfProva, pdfGabarito);

        return ResponseEntity.ok(new IngestaoResponse(
                simulado.getId(), simulado.getTitulo(), simulado.getQuestoes().size()));
    }
}