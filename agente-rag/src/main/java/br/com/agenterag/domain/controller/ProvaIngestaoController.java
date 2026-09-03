package br.com.agenterag.domain.controller;

import br.com.agenterag.domain.dto.IngestaoRequest;
import br.com.agenterag.domain.dto.IngestaoResponse;
import br.com.agenterag.domain.internal.Simulado;
import br.com.agenterag.domain.service.SimuladoOrchestratorService;
import br.com.agenterag.ingestion.service.PdfDownloadService;
import org.springframework.http.HttpStatus;
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

        // Nunca confia só no nomeArquivo do body: se vier em branco/nulo, ele
        // vira "IS NULL" na query de deduplicação (findByNomeArquivo) e passa
        // a colidir com QUALQUER outra prova ingerida do mesmo jeito — foi
        // exatamente isso que fez a ingestão da XXI ser tratada como
        // duplicata da XX. Deriva sempre da URL, que é única por natureza.
        String nomeArquivo = (request.nomeArquivo() == null || request.nomeArquivo().isBlank())
                ? extrairNomeArquivo(request.urlProva())
                : request.nomeArquivo();

        Simulado simulado = orchestrator.ingerirEGerarSimulado(
                request.edicao(), nomeArquivo, pdfProva, pdfGabarito);

        IngestaoResponse response = new IngestaoResponse(
                simulado.getId(), simulado.getTitulo(), simulado.getQuestoes().size());

        if (simulado.getQuestoes().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }

        return ResponseEntity.ok(response);
    }

    private String extrairNomeArquivo(String url) {
        int barra = url.lastIndexOf('/');
        return barra >= 0 ? url.substring(barra + 1) : url;
    }
}