package br.com.agenterag;

import br.com.agenterag.domain.internal.Questao;
import br.com.agenterag.domain.service.PdfParserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class GeradorJsonDiretoDoPdf {

    public static void main(String[] args) throws Exception {
        // Lê o PDF diretamente da pasta downloads-teste
        byte[] pdfBytes = Files.readAllBytes(Paths.get("downloads-teste/Caderno_de_Prova___Tipo_1.pdf"));

        // Usa o ParserService que já tem suporte a Tesseract
        PdfParserService parser = new PdfParserService();
        List<Questao> questoes = parser.extrairQuestoes(pdfBytes);

        // Converte para JSON
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(questoes);

        // Salva
        Files.writeString(Paths.get("questoes.json"), json);

        System.out.println("✅ JSON gerado com " + questoes.size() + " questões!");
        System.out.println("📁 Arquivo salvo em: " + Paths.get("questoes.json").toAbsolutePath());
    }
}