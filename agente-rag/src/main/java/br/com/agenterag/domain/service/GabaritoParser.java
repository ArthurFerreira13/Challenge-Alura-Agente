package br.com.agenterag.domain.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GabaritoParser {

    private static final Logger log = LoggerFactory.getLogger(GabaritoParser.class);

    // Identifica o início de cada bloco "PROVA N" (Tipo 1, 2, 3 ou 4)
    private static final Pattern PROVA_HEADER =
            Pattern.compile("(?i)PROVA\\s+(?:TIPO\\s+)?(\\d)");

    // Uma linha só de números (cabeçalho de coluna: "1 2 3 ... 20")
    private static final Pattern LINHA_NUMEROS =
            Pattern.compile("^\\s*(\\d{1,3}(?:\\s+\\d{1,3}){3,})\\s*$");

    // Uma linha só de letras A-D e/ou "*" (linha de respostas)
    private static final Pattern LINHA_RESPOSTAS =
            Pattern.compile("^\\s*([A-D*](?:\\s+[A-D*]){3,})\\s*$");

    public static final String ANULADA = "ANULADA";

    /**
     * Extrai o gabarito de um tipo específico de caderno (1 a 4).
     * Recomendado: sempre usar tipoProva = 1 (Caderno Branco), casando
     * com a prova baixada também sempre como Tipo 1 — evita depender
     * da tabela de correspondência entre tipos.
     */
    public Map<Integer, String> extrairRespostas(byte[] pdfBytes, int tipoProva) {
        String texto;
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            texto = stripper.getText(document);
        } catch (IOException e) {
            log.error("Falha ao ler o PDF do gabarito", e);
            return Map.of();
        }

        String blocoDoTipo = isolarBlocoDoTipo(texto, tipoProva);
        if (blocoDoTipo == null) {
            log.warn("Não encontrei o bloco 'PROVA {}' no gabarito", tipoProva);
            return Map.of();
        }

        Map<Integer, String> respostas = new LinkedHashMap<>();
        String[] linhas = blocoDoTipo.split("\\r?\\n");

        for (int i = 0; i < linhas.length - 1; i++) {
            Matcher numMatcher = LINHA_NUMEROS.matcher(linhas[i]);
            if (!numMatcher.matches()) continue;

            Matcher letraMatcher = LINHA_RESPOSTAS.matcher(linhas[i + 1]);
            if (!letraMatcher.matches()) continue;

            String[] numeros = numMatcher.group(1).trim().split("\\s+");
            String[] letras = letraMatcher.group(1).trim().split("\\s+");

            if (numeros.length != letras.length) {
                log.warn("Linha de números ({} itens) não bate com linha de respostas ({} itens) — pulando",
                        numeros.length, letras.length);
                continue;
            }

            for (int j = 0; j < numeros.length; j++) {
                int numero = Integer.parseInt(numeros[j]);
                String letra = "*".equals(letras[j]) ? ANULADA : letras[j];
                respostas.put(numero, letra);
            }
        }

        log.info("Gabarito Tipo {}: {} respostas extraídas ({} anuladas)",
                tipoProva, respostas.size(),
                respostas.values().stream().filter(ANULADA::equals).count());

        return respostas;
    }

    private String isolarBlocoDoTipo(String texto, int tipoProva) {
        Matcher m = PROVA_HEADER.matcher(texto);
        int inicio = -1;
        while (m.find()) {
            if (Integer.parseInt(m.group(1)) == tipoProva) {
                inicio = m.end();
                break;
            }
        }
        if (inicio == -1) return null;

        // Bloco vai até o próximo "PROVA N" ou até a "TABELA DE CORRESPONDÊNCIA"
        Matcher proximo = PROVA_HEADER.matcher(texto);
        int fim = texto.length();
        if (proximo.find(inicio)) {
            fim = proximo.start();
        }
        return texto.substring(inicio, fim);
    }
}
