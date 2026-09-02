package br.com.agenterag.domain.dto;

public record IngestaoResponse(
        Long simuladoId,
        String titulo,
        int totalQuestoes
) {}