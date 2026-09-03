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
public class CnsParsingStrategy implements ParsingStrategy {

    @Override
    public boolean supports(String nomeArquivo, String urlProva) {
        boolean suporta = nomeArquivo != null && nomeArquivo.matches(".*CNS\\d+.*");
        System.out.println("CnsParsingStrategy.supports(" + nomeArquivo + ") = " + suporta);
        return suporta;
    }

    @Override
    public List<QuestaoExtracted> parseQuestoes(PDDocument document) throws Exception {
        System.out.println("CnsParsingStrategy.parseQuestoes() iniciado.");

        // Extrai texto com configurações que melhoram a estrutura
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        stripper.setLineSeparator("\n");
        stripper.setWordSeparator(" ");
        stripper.setStartPage(1);
        stripper.setEndPage(document.getNumberOfPages());
        String texto = stripper.getText(document);

        System.out.println("Texto extraído: " + texto.length() + " caracteres.");
        System.out.println("Primeiros 500 caracteres:\n" + texto.substring(0, Math.min(500, texto.length())));
        System.out.println("Primeiros 2000 caracteres:\n" + texto.substring(0, Math.min(2000, texto.length())));

        // 1. Remove cabeçalhos e rodapés indesejados (opcional)
        texto = texto.replaceAll("(?m)^.*?EXAME DE ORDEM UNIFICADO.*$", "")
                .replaceAll("(?m)^.*?SUA PROVA.*$", "")
                .replaceAll("(?m)^\\s*\\d+\\s*$", ""); // remove números de página soltos

        // 2. Regex para capturar blocos de questões: "X. texto até o próximo X."
        // A regex usa lookahead para capturar o próximo número.
        Pattern pattern = Pattern.compile("(\\d{1,2})\\.\\s+(.*?)(?=\\s*\\d{1,2}\\.\\s+|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(texto);

        List<QuestaoExtracted> questoes = new ArrayList<>();
        List<Integer> numeros = new ArrayList<>();

        while (matcher.find()) {
            Integer numero = Integer.parseInt(matcher.group(1));
            if (numero < 1 || numero > 80) continue; // ignora números fora do intervalo
            String bloco = matcher.group(2).trim();
            if (bloco.length() < 10) continue; // ignora blocos muito curtos (ex: números de página)

            numeros.add(numero);
            QuestaoExtracted q = criarQuestao(numero, bloco);
            if (q != null) {
                questoes.add(q);
            }
        }

        System.out.println("Números das questões encontradas: " + numeros);
        System.out.println("Total de questões extraídas (CNS): " + questoes.size());

        // Se não encontrou todas, tenta um fallback buscando por "A)", "B)", etc.
        if (questoes.size() < 80) {
            System.out.println("Buscando questões faltantes via fallback de alternativas...");
            List<QuestaoExtracted> faltantes = extrairPorAlternativas(texto, numeros);
            for (QuestaoExtracted q : faltantes) {
                if (!numeros.contains(q.numero())) {
                    questoes.add(q);
                    numeros.add(q.numero());
                }
            }
            System.out.println("Após fallback, total de questões: " + questoes.size());
        }

        return questoes;
    }

    private QuestaoExtracted criarQuestao(Integer numero, String bloco) {
        // Extrai alternativas (formato (A), A), A.)
        List<AlternativaExtracted> alternativas = extrairAlternativas(bloco);
        String enunciado = bloco;

        if (!alternativas.isEmpty()) {
            // Encontra a posição da primeira alternativa para isolar o enunciado
            int menorPos = Integer.MAX_VALUE;
            for (AlternativaExtracted alt : alternativas) {
                int pos = bloco.indexOf(alt.letra() + ")");
                if (pos == -1) pos = bloco.indexOf(alt.letra() + ".");
                if (pos == -1) pos = bloco.indexOf("(" + alt.letra() + ")");
                if (pos != -1 && pos < menorPos) menorPos = pos;
            }
            if (menorPos != Integer.MAX_VALUE && menorPos > 0) {
                enunciado = bloco.substring(0, menorPos).trim();
            }
        }

        // Se não encontrou alternativas, pode ser que as alternativas estejam em formato diferente
        // ou a questão seja apenas enunciado (gabarito separado). Mantemos o bloco completo.
        return new QuestaoExtracted(numero, enunciado, alternativas, null);
    }

    private List<AlternativaExtracted> extrairAlternativas(String texto) {
        List<AlternativaExtracted> alternativas = new ArrayList<>();
        Pattern[] patterns = {
                Pattern.compile("\\(([A-D])\\)\\s*(.*?)(?=\\([A-D]\\)|$)", Pattern.DOTALL),
                Pattern.compile("([A-D])\\)\\s*(.*?)(?=[A-D]\\)|$)", Pattern.DOTALL),
                Pattern.compile("([A-D])\\.\\s*(.*?)(?=[A-D]\\.|$)", Pattern.DOTALL)
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(texto);
            if (m.find()) {
                m.reset();
                while (m.find()) {
                    String letra = m.group(1);
                    String textoAlt = m.group(2).trim();
                    if (!textoAlt.isEmpty()) {
                        alternativas.add(new AlternativaExtracted(letra, textoAlt));
                    }
                }
                break;
            }
        }

        // Fallback: procura por "A)", "B)", etc. mesmo sem espaços
        if (alternativas.isEmpty()) {
            String[] letras = {"A", "B", "C", "D"};
            for (String letra : letras) {
                int idx = texto.indexOf(letra + ")");
                if (idx == -1) idx = texto.indexOf(letra + ".");
                if (idx != -1) {
                    int nextIdx = texto.length();
                    for (String outra : letras) {
                        if (outra.equals(letra)) continue;
                        int temp = texto.indexOf(outra + ")");
                        if (temp == -1) temp = texto.indexOf(outra + ".");
                        if (temp != -1 && temp < nextIdx) nextIdx = temp;
                    }
                    String textoAlt = texto.substring(idx + (texto.charAt(idx+1) == ')' ? 2 : 1), nextIdx).trim();
                    if (!textoAlt.isEmpty()) {
                        alternativas.add(new AlternativaExtracted(letra, textoAlt));
                    }
                }
            }
        }

        return alternativas;
    }

    // Fallback que busca blocos contendo alternativas para questões faltantes
    private List<QuestaoExtracted> extrairPorAlternativas(String texto, List<Integer> jaEncontrados) {
        List<QuestaoExtracted> questoes = new ArrayList<>();
        String[] blocos = texto.split("(?=\\s*[A-D]\\)\\s+)");
        int questaoAtual = 1;
        for (String bloco : blocos) {
            if (bloco.trim().isEmpty()) continue;
            // Tenta identificar o número da questão a partir do conteúdo
            Matcher m = Pattern.compile("\\b(\\d{1,2})\\b").matcher(bloco);
            if (m.find()) {
                int num = Integer.parseInt(m.group(1));
                if (num >= 1 && num <= 80 && !jaEncontrados.contains(num)) {
                    QuestaoExtracted q = criarQuestao(num, bloco);
                    if (q != null) {
                        questoes.add(q);
                    }
                }
            } else {
                // Se não encontrou número, usa o contador
                if (questaoAtual <= 80 && !jaEncontrados.contains(questaoAtual)) {
                    QuestaoExtracted q = criarQuestao(questaoAtual, bloco);
                    if (q != null) {
                        questoes.add(q);
                    }
                }
                questaoAtual++;
            }
        }
        return questoes;
    }
}