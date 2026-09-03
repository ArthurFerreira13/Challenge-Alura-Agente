package br.com.agenterag.domain.service;

import br.com.agenterag.ingestion.dto.QuestaoExtracted;
import br.com.agenterag.ingestion.strategy.AlternativeParsingStrategy;
import br.com.agenterag.ingestion.strategy.CnsParsingStrategy;
import br.com.agenterag.ingestion.strategy.EouParsingStrategy;
import br.com.agenterag.ingestion.strategy.ParsingStrategy;
import br.com.agenterag.ingestion.strategy.ParsingStrategyFactory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PdfParserService {

    private static final Logger log = LoggerFactory.getLogger(PdfParserService.class);

    private final ParsingStrategyFactory strategyFactory;

    public PdfParserService(ParsingStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    /**
     * Extrai as questões do PDF usando uma cadeia de fallbacks.
     * Ordem:
     * 1. Se nome contém "EOU" → EouParsingStrategy
     * 2. Se nome contém "CNS" seguido de números → CnsParsingStrategy
     * 3. Fallback genérico → AlternativeParsingStrategy (tenta múltiplos padrões)
     * 4. Último recurso → fábrica de estratégias (caso haja outras registradas)
     */
    public List<QuestaoExtracted> extrairQuestoes(byte[] pdfBytes, String nomeArquivo, String urlProva) throws Exception {
        log.info("Extraindo questões para: {}", nomeArquivo);

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            List<QuestaoExtracted> questoes = new ArrayList<>();

            // 1. Estratégia EOU (padrão "QUESTÃO X")
            if (nomeArquivo != null && nomeArquivo.contains("EOU")) {
                log.info("Tentando EouParsingStrategy...");
                EouParsingStrategy eouStrategy = new EouParsingStrategy();
                questoes = eouStrategy.parseQuestoes(document);
                log.info("EouParsingStrategy extraiu {} questões.", questoes.size());
            }

            // 2. Estratégia CNS (numeração "X.")
            if (questoes.isEmpty() && nomeArquivo != null && nomeArquivo.matches(".*CNS\\d+.*")) {
                log.info("Tentando CnsParsingStrategy...");
                CnsParsingStrategy cnsStrategy = new CnsParsingStrategy();
                questoes = cnsStrategy.parseQuestoes(document);
                log.info("CnsParsingStrategy extraiu {} questões.", questoes.size());
            }

            // 3. Estratégia Alternativa (formatos variados: Q. X, Q X, X), negrito, etc.)
            if (questoes.isEmpty()) {
                log.info("Tentando AlternativeParsingStrategy (fallback genérico)...");
                AlternativeParsingStrategy altStrategy = new AlternativeParsingStrategy();
                questoes = altStrategy.parseQuestoes(document);
                log.info("AlternativeParsingStrategy extraiu {} questões.", questoes.size());
            }

            // 4. Último recurso: usar a fábrica (caso existam outras estratégias registradas)
            if (questoes.isEmpty()) {
                log.warn("Nenhuma estratégia específica encontrou questões. Tentando ParsingStrategyFactory...");
                try {
                    ParsingStrategy factoryStrategy = strategyFactory.getStrategy(nomeArquivo, urlProva);
                    questoes = factoryStrategy.parseQuestoes(document);
                    log.info("ParsingStrategyFactory extraiu {} questões.", questoes.size());
                } catch (Exception e) {
                    log.error("Falha ao usar ParsingStrategyFactory", e);
                }
            }

            // Se ainda não encontrou questões, log de erro
            if (questoes.isEmpty()) {
                log.error("NENHUMA QUESTÃO FOI EXTRAÍDA para '{}' após todas as tentativas.", nomeArquivo);
            } else {
                log.info("Total final de questões extraídas para '{}': {}", nomeArquivo, questoes.size());
            }

            return questoes;
        }
    }
}