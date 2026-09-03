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
public class AlternativeParsingStrategy implements ParsingStrategy {

    @Override
    public boolean supports(String nomeArquivo, String urlProva) {
        // Esta estratégia é usada como fallback para qualquer arquivo que não seja EOU nem CNS
        // Então supports sempre retorna true, mas será usada em último caso.
        return true;
    }

    @Override
    public List<QuestaoExtracted> parseQuestoes(PDDocument document) throws Exception {
        System.out.println("AlternativeParsingStrategy.parseQuestoes() iniciado.");

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        String texto = stripper.getText(document);

        System.out.println("Texto extraído: " + texto.length() + " caracteres.");

        List<QuestaoExtracted> questoes = new ArrayList<>();

        // --- PADRÃO 1: "Q. X" (Q. 1, Q. 2, ...) ---
        Pattern p1 = Pattern.compile("(?:Q\\.\\s*)(\\d+)\\s*[-–]?\\s*(.*?)(?=(?:Q\\.\\s*\\d+|$))", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m1 = p1.matcher(texto);
        while (m1.find()) {
            questoes.add(criarQuestao(Integer.parseInt(m1.group(1)), m1.group(2).trim()));
        }
        System.out.println("Padrão 'Q. X': " + questoes.size() + " questões.");

        // --- PADRÃO 2: "Q X" (Q 1, Q 2, ...) ---
        if (questoes.size() < 80) {
            Pattern p2 = Pattern.compile("(?:Q\\s+)(\\d+)\\s*[-–]?\\s*(.*?)(?=(?:Q\\s+\\d+|$))", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher m2 = p2.matcher(texto);
            List<QuestaoExtracted> temp = new ArrayList<>();
            while (m2.find()) {
                temp.add(criarQuestao(Integer.parseInt(m2.group(1)), m2.group(2).trim()));
            }
            if (temp.size() > questoes.size()) {
                questoes = temp;
            }
            System.out.println("Padrão 'Q X': " + questoes.size() + " questões.");
        }

        // --- PADRÃO 3: "X)" no início da linha (já tínhamos, mas vamos reforçar) ---
        if (questoes.size() < 80) {
            Pattern p3 = Pattern.compile("^(\\d+)\\)\\s+(.*?)(?=^\\d+\\)\\s+|$)", Pattern.MULTILINE | Pattern.DOTALL);
            Matcher m3 = p3.matcher(texto);
            List<QuestaoExtracted> temp = new ArrayList<>();
            while (m3.find()) {
                temp.add(criarQuestao(Integer.parseInt(m3.group(1)), m3.group(2).trim()));
            }
            if (temp.size() > questoes.size()) {
                questoes = temp;
            }
            System.out.println("Padrão 'X)': " + questoes.size() + " questões.");
        }

        // --- PADRÃO 4: números com negrito (**1**) ---
        if (questoes.size() < 80) {
            Pattern p4 = Pattern.compile("\\*{0,2}(\\d+)\\*{0,2}\\s*[-–]?\\s*(.*?)(?=\\*{0,2}\\d+\\*{0,2}\\s|$)", Pattern.DOTALL);
            Matcher m4 = p4.matcher(texto);
            List<QuestaoExtracted> temp = new ArrayList<>();
            while (m4.find()) {
                int num = Integer.parseInt(m4.group(1));
                if (num >= 1 && num <= 80) {
                    temp.add(criarQuestao(num, m4.group(2).trim()));
                }
            }
            if (temp.size() > questoes.size()) {
                questoes = temp;
            }
            System.out.println("Padrão '**X**': " + questoes.size() + " questões.");
        }

        // --- PADRÃO 5: global "X." (qualquer lugar, com ponto) ---
        if (questoes.size() < 80) {
            Pattern p5 = Pattern.compile("(\\d{1,2})\\.\\s+(.*?)(?=\\s*\\d{1,2}\\.\\s+|$)", Pattern.DOTALL);
            Matcher m5 = p5.matcher(texto);
            List<QuestaoExtracted> temp = new ArrayList<>();
            while (m5.find()) {
                int num = Integer.parseInt(m5.group(1));
                if (num >= 1 && num <= 80) {
                    temp.add(criarQuestao(num, m5.group(2).trim()));
                }
            }
            if (temp.size() > questoes.size()) {
                questoes = temp;
            }
            System.out.println("Padrão 'X.' (global): " + questoes.size() + " questões.");
        }

        // --- PADRÃO 6: global "X) " ---
        if (questoes.size() < 80) {
            Pattern p6 = Pattern.compile("(\\d{1,2})\\)\\s+(.*?)(?=\\s*\\d{1,2}\\)\\s+|$)", Pattern.DOTALL);
            Matcher m6 = p6.matcher(texto);
            List<QuestaoExtracted> temp = new ArrayList<>();
            while (m6.find()) {
                int num = Integer.parseInt(m6.group(1));
                if (num >= 1 && num <= 80) {
                    temp.add(criarQuestao(num, m6.group(2).trim()));
                }
            }
            if (temp.size() > questoes.size()) {
                questoes = temp;
            }
            System.out.println("Padrão 'X)' (global): " + questoes.size() + " questões.");
        }

        System.out.println("Total de questões extraídas (Alternative): " + questoes.size());
        return questoes;
    }

    private QuestaoExtracted criarQuestao(Integer numero, String bloco) {
        List<AlternativaExtracted> alternativas = extrairAlternativas(bloco);
        String enunciado = bloco;
        if (!alternativas.isEmpty()) {
            int firstAltIndex = bloco.indexOf("(A)");
            if (firstAltIndex == -1) firstAltIndex = bloco.indexOf("A)");
            if (firstAltIndex == -1) firstAltIndex = bloco.indexOf("A.");
            if (firstAltIndex > 0) {
                enunciado = bloco.substring(0, firstAltIndex).trim();
            }
        }
        return new QuestaoExtracted(numero, enunciado, alternativas, null);
    }

    private List<AlternativaExtracted> extrairAlternativas(String texto) {
        List<AlternativaExtracted> list = new ArrayList<>();
        Pattern p = Pattern.compile("\\(([A-E])\\)\\s*(.*?)(?=\\([A-E]\\)|$)", Pattern.DOTALL);
        Matcher m = p.matcher(texto);
        if (m.find()) {
            m.reset();
            while (m.find()) list.add(new AlternativaExtracted(m.group(1), m.group(2).trim()));
        } else {
            p = Pattern.compile("([A-E])\\)\\s*(.*?)(?=[A-E]\\)|$)", Pattern.DOTALL);
            m = p.matcher(texto);
            if (m.find()) {
                m.reset();
                while (m.find()) list.add(new AlternativaExtracted(m.group(1), m.group(2).trim()));
            } else {
                p = Pattern.compile("([A-E])\\.\\s*(.*?)(?=[A-E]\\.|$)", Pattern.DOTALL);
                m = p.matcher(texto);
                if (m.find()) {
                    m.reset();
                    while (m.find()) list.add(new AlternativaExtracted(m.group(1), m.group(2).trim()));
                }
            }
        }
        return list;
    }
}