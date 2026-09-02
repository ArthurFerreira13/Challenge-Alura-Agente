package br.com.agenterag.domain.dto;

public record IngestaoRequest(
        String edicao,
        String nomeArquivo,
        String urlProva,
        String urlGabarito
) {}