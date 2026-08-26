package br.com.agenterag.domain.service;

import br.com.agenterag.domain.internal.Questao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PdfParserService {

    public List<Questao> extrairQuestoes(byte[] pdfBytes) {
        // Lógica de extração do PDFBox / LangChain4j virá aqui
        return List.of();
    }
}
