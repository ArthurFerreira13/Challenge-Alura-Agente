package br.com.agenterag.domain.service;

import br.com.agenterag.domain.internal.Alternativa;
import br.com.agenterag.domain.internal.Questao;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfParserService {

    private static final Logger log = LoggerFactory.getLogger(PdfParserService.class);

    // Aceita tanto "Questão 1" (formato antigo) quanto apenas "1" sozinho na
    // linha (formato usado em exames mais recentes, como o XXXVII) — a palavra
    // "Questão" é opcional.
    private static final Pattern QUESTAO_SPLIT =
            Pattern.compile("(?imu)^\\s*(?:QUEST[ÃA]O\\s*(?:N[ºO°]?\\s*)?)?(\\d{1,3})\\s*$");

    private static final Pattern ALTERNATIVA_SPLIT =
            Pattern.compile("(?m)^\\s*\\(?([A-D])\\)?[).]\\s+");

    private static final Pattern CABECALHO_PAGINA =
            Pattern.compile("(?imu)^\\s*[IVXLCDM]+\\s+EXAME DE ORDEM UNIFICADO.*$");

    private static final Pattern LINHA_DATA_PROVA =
            Pattern.compile("(?im)^\\s*PROVA APLICADA EM \\d{1,2}/\\d{1,2}/\\d{2,4}\\s*$");

    // Corta o texto aqui — tudo depois é o questionário de satisfação (10
    // itens numerados de 1 a 10, mesmo formato de alternativa), que colidiria
    // com as questões reais 1-10 se não for removido antes do parsing.
    private static final Pattern LIMITE_QUESTIONARIO_PERCEPCAO =
            Pattern.compile("(?imu)QUESTION[ÁA]RIO\\s+DE\\s+PERCEP[ÇC][ÃA]O");

    public List<Questao> extrairQuestoes(byte[] pdfBytes) {
        String texto = extrairTextoDoPdf(pdfBytes);
        return parsearTexto(texto);
    }

    private String extrairTextoDoPdf(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("Falha ao ler o PDF para extração de questões", e);
            return "";
        }
    }

    List<Questao> parsearTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            log.warn("Texto vazio recebido para parsing.");
            return List.of();
        }

        texto = cortarQuestionarioPercepcao(texto);
        texto = limparRuido(texto);

        List<Questao> questoes = new ArrayList<>();
        Matcher questaoMatcher = QUESTAO_SPLIT.matcher(texto);
        List<Integer> numeros = new ArrayList<>();
        List<Integer> posicoesInicio = new ArrayList<>();

        while (questaoMatcher.find()) {
            numeros.add(Integer.parseInt(questaoMatcher.group(1)));
            posicoesInicio.add(questaoMatcher.end());
        }

        log.info("Candidatos a cabeçalho de questão encontrados: {}", posicoesInicio.size());

        for (int i = 0; i < posicoesInicio.size(); i++) {
            int inicio = posicoesInicio.get(i);
            int fim = (i + 1 < posicoesInicio.size())
                    ? indicioDoProximoMatch(texto, posicoesInicio.get(i))
                    : texto.length();

            String bloco = texto.substring(inicio, Math.min(fim, texto.length())).trim();
            Questao questao = parseBloco(numeros.get(i), bloco);
            if (questao != null) {
                questoes.add(questao);
            }
        }

        log.info("Extração concluída: {} questões identificadas ({} caracteres de texto)",
                questoes.size(), texto.length());

        return questoes;
    }

    private String cortarQuestionarioPercepcao(String texto) {
        Matcher m = LIMITE_QUESTIONARIO_PERCEPCAO.matcher(texto);
        if (m.find()) {
            return texto.substring(0, m.start());
        }
        return texto;
    }

    private String limparRuido(String texto) {
        texto = CABECALHO_PAGINA.matcher(texto).replaceAll("");
        texto = LINHA_DATA_PROVA.matcher(texto).replaceAll("");
        // Nota: números soltos NÃO são mais removidos aqui — agora são o
        // próprio delimitador de questão (ver QUESTAO_SPLIT). Números de
        // página se auto-filtram no parseBloco, por não terem 4 alternativas.
        return texto;
    }

    private int indicioDoProximoMatch(String texto, int aPartirDe) {
        Matcher m = QUESTAO_SPLIT.matcher(texto);
        return m.find(aPartirDe) ? m.start() : texto.length();
    }

    private Questao parseBloco(int numero, String bloco) {
        Matcher altMatcher = ALTERNATIVA_SPLIT.matcher(bloco);

        List<String> letras = new ArrayList<>();
        List<Integer> posicoesLetra = new ArrayList<>();
        while (altMatcher.find()) {
            letras.add(altMatcher.group(1));
            posicoesLetra.add(altMatcher.end());
        }

        if (letras.size() < 4) {
            // Rejeita silenciosamente — cobre tanto ruído real (números de
            // página) quanto blocos truncados. Não logamos em nível WARN
            // aqui porque, com o novo formato, isso é esperado e frequente
            // (todo número de página vira um candidato descartado).
            log.debug("Bloco do candidato {} descartado: {} alternativa(s) encontrada(s)", numero, letras.size());
            return null;
        }

        int fimEnunciado = altMatcher.reset(bloco).find() ? altMatcher.start() : bloco.length();
        String enunciado = bloco.substring(0, fimEnunciado).trim();

        Questao questao = new Questao();
        questao.setNumeroQuestao(numero);
        questao.setEnunciado(enunciado);
        questao.setDisciplina(DisciplinaMapper.paraQuestao(numero));

        List<Alternativa> alternativas = new ArrayList<>();
        for (int i = 0; i < letras.size(); i++) {
            int inicio = posicoesLetra.get(i);
            int fim = (i + 1 < posicoesLetra.size())
                    ? posicaoAntesDaProximaLetra(bloco, posicoesLetra.get(i))
                    : bloco.length();
            String textoAlt = bloco.substring(inicio, Math.min(fim, bloco.length())).trim();

            Alternativa alt = new Alternativa();
            alt.setLetra(letras.get(i));
            alt.setTexto(textoAlt);
            alt.setQuestao(questao);
            alternativas.add(alt);
        }
        questao.setAlternativas(alternativas);

        return questao;
    }

    private int posicaoAntesDaProximaLetra(String bloco, int aPartirDe) {
        Matcher m = ALTERNATIVA_SPLIT.matcher(bloco);
        return m.find(aPartirDe) ? m.start() : bloco.length();
    }
}