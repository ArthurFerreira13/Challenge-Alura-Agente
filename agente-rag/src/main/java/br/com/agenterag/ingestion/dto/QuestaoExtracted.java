package br.com.agenterag.ingestion.dto;

import java.util.List;

public record QuestaoExtracted(
        Integer numero,
        String enunciado,
        List<AlternativaExtracted> alternativas,
        String alternativaCorreta
) {
}