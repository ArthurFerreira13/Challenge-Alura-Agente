package br.com.agenterag.ingestion.strategy;

import br.com.agenterag.ingestion.dto.QuestaoExtracted;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.List;

public interface ParsingStrategy {
    boolean supports(String nomeArquivo, String urlProva);
    List<QuestaoExtracted> parseQuestoes(PDDocument document) throws Exception;
}
