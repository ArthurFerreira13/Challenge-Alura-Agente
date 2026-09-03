package br.com.agenterag.ingestion.strategy;

import br.com.agenterag.ingestion.dto.AlternativaExtracted;
import br.com.agenterag.ingestion.dto.QuestaoExtracted;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EouParsingStrategy implements ParsingStrategy {

    @Override
    public boolean supports(String nomeArquivo, String urlProva) {
        return nomeArquivo != null && nomeArquivo.contains("EOU");
    }

    @Override
    public List<QuestaoExtracted> parseQuestoes(PDDocument document) throws Exception {
        PDFTextStripper stripper = new PDFTextStripper();
        String texto = stripper.getText(document);

        List<QuestaoExtracted> questoes = new ArrayList<>();

        Pattern pattern = Pattern.compile("(?:QUEST[ÃA]O\\s+)(\\d+)\\s*[-–]?\\s*(.*?)(?=(?:QUEST[ÃA]O\\s+\\d+|$))", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(texto);

        while (matcher.find()) {
            Integer numero = Integer.parseInt(matcher.group(1));
            String conteudo = matcher.group(2).trim();

            List<AlternativaExtracted> alternativas = extrairAlternativas(conteudo);
            String enunciado = conteudo;
            if (!alternativas.isEmpty()) {
                int firstAltIndex = conteudo.indexOf("(A)");
                if (firstAltIndex > 0) {
                    enunciado = conteudo.substring(0, firstAltIndex).trim();
                }
            }
            questoes.add(new QuestaoExtracted(numero, enunciado, alternativas, null));
        }

        return questoes;
    }

    private List<AlternativaExtracted> extrairAlternativas(String texto) {
        List<AlternativaExtracted> list = new ArrayList<>();
        Pattern altPattern = Pattern.compile("\\(([A-E])\\)\\s*(.*?)(?=\\([A-E]\\)|$)", Pattern.DOTALL);
        Matcher m = altPattern.matcher(texto);
        while (m.find()) {
            list.add(new AlternativaExtracted(m.group(1), m.group(2).trim()));
        }
        return list;
    }
}