 /*package br.com.agenterag.domain.service;

import br.com.agenterag.domain.dto.ExameFonte;
import br.com.agenterag.domain.dto.ResultadoIngestao;
import br.com.agenterag.ingestion.dto.ItemProvaFgv;
import br.com.agenterag.ingestion.service.PdfDownloadService;
import br.com.agenterag.ingestion.service.ScraperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BatchIngestionService {

    private static final Logger log = LoggerFactory.getLogger(BatchIngestionService.class);

    // Delay entre exames, pra não sobrecarregar o servidor da FGV
    // nem correr risco de bloqueio de IP por excesso de requisições.
    private static final long DELAY_ENTRE_EXAMES_MS = 2000;

    private final ScraperService scraperService;
    private final PdfDownloadService downloadService;
    private final SimuladoOrchestratorService orchestrator;

    public BatchIngestionService(ScraperService scraperService,
                                 PdfDownloadService downloadService,
                                 SimuladoOrchestratorService orchestrator) {
        this.scraperService = scraperService;
        this.downloadService = downloadService;
        this.orchestrator = orchestrator;
    }

    public List<ResultadoIngestao> processarLote(List<ExameFonte> exames) {
        List<ResultadoIngestao> resultados = new ArrayList<>();

        for (int i = 0; i < exames.size(); i++) {
            ExameFonte exame = exames.get(i);
            log.info("=== [{}/{}] Processando: {} ===", i + 1, exames.size(), exame.edicao());

            ResultadoIngestao resultado = processarUmExame(exame);
            resultados.add(resultado);

            if (resultado.sucesso()) {
                log.info("✔ {}: {}", exame.edicao(), resultado.mensagem());
            } else {
                log.warn("✘ {}: {}", exame.edicao(), resultado.mensagem());
            }

            if (i < exames.size() - 1) {
                aguardar(DELAY_ENTRE_EXAMES_MS);
            }
        }

        imprimirResumo(resultados);
        return resultados;
    }

    private ResultadoIngestao processarUmExame(ExameFonte exame) {
        try {
            String urlProva;
            String urlGabarito;

            if (exame.temUrlsDiretas()) {
                urlProva = exame.urlProvaDireta();
                urlGabarito = exame.urlGabaritoDireto();
            } else {
                Optional<String[]> urls = descobrirUrlsViaScraper(exame.urlSecaoFgv());
                if (urls.isEmpty()) {
                    return ResultadoIngestao.falha(exame.edicao(),
                            "Não encontrei Caderno Tipo 1 e/ou gabarito na página da seção");
                }
                urlProva = urls.get()[0];
                urlGabarito = urls.get()[1];
            }

            byte[] pdfProva = downloadService.baixarPdfParaBytes(urlProva);
            byte[] pdfGabarito = downloadService.baixarPdfParaBytes(urlGabarito);

            var simulado = orchestrator.ingerirEGerarSimulado(
                    exame.edicao(), extrairNomeArquivo(urlProva), pdfProva, pdfGabarito);

            if (simulado.getQuestoes().isEmpty()) {
                return ResultadoIngestao.falha(exame.edicao(),
                        "Ingestão rodou sem erro, mas 0 questões foram salvas (provável falha de parsing)");
            }

            return ResultadoIngestao.sucesso(exame.edicao(), simulado.getQuestoes().size());

        } catch (PdfParserService.TextoIlegivelException e) {
            return ResultadoIngestao.falha(exame.edicao(), "PDF ilegível: " + e.getMessage());
        } catch (Exception e) {
            // Captura ampla de propósito: qualquer falha (download, parsing,
            // banco) não pode derrubar o processamento dos outros exames do lote.
            log.error("Erro inesperado processando '{}'", exame.edicao(), e);
            return ResultadoIngestao.falha(exame.edicao(),
                    e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private Optional<String[]> descobrirUrlsViaScraper(String urlSecao) throws Exception {
        List<ItemProvaFgv> itens = scraperService.extrairItensDaPagina(urlSecao);

        String urlProva = itens.stream()
                .filter(i -> !i.ehGabarito())
                .filter(i -> i.titulo().toLowerCase().contains("tipo 1"))
                .map(ItemProvaFgv::urlPdf)
                .findFirst()
                .orElse(null);

        // Prioriza gabarito DEFINITIVO sobre PRELIMINAR
        String urlGabarito = itens.stream()
                .filter(ItemProvaFgv::ehGabarito)
                .filter(i -> i.titulo().toLowerCase().contains("1ª fase")
                        || i.titulo().toLowerCase().contains("1a fase")
                        || i.titulo().toLowerCase().contains("prova objetiva"))
                .sorted((a, b) -> Boolean.compare(
                        !a.titulo().toLowerCase().contains("definitivo"),
                        !b.titulo().toLowerCase().contains("definitivo")))
                .map(ItemProvaFgv::urlPdf)
                .findFirst()
                .orElse(null);

        if (urlProva == null || urlGabarito == null) {
            return Optional.empty();
        }
        return Optional.of(new String[]{urlProva, urlGabarito});
    }

    private String extrairNomeArquivo(String url) {
        int barra = url.lastIndexOf('/');
        return barra >= 0 ? url.substring(barra + 1) : url;
    }

    private void aguardar(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void imprimirResumo(List<ResultadoIngestao> resultados) {
        long sucessos = resultados.stream().filter(ResultadoIngestao::sucesso).count();
        int totalQuestoes = resultados.stream().mapToInt(ResultadoIngestao::questoesSalvas).sum();

        log.info("========== RESUMO DO LOTE ==========");
        log.info("Exames processados: {}", resultados.size());
        log.info("Sucessos: {} | Falhas: {}", sucessos, resultados.size() - sucessos);
        log.info("Total de questões salvas no lote: {}", totalQuestoes);
        resultados.stream()
                .filter(r -> !r.sucesso())
                .forEach(r -> log.info("  FALHOU: {} — {}", r.edicao(), r.mensagem()));
        log.info("=====================================");
    }
} */