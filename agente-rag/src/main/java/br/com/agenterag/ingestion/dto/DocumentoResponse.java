package br.com.agenterag.ingestion.dto;

import br.com.agenterag.core.domain.Documento;

import java.time.Instant;

public record DocumentoResponse(
        Long id,
        String nomeArquivo,
        String extensao,
        String categoria,
        int totalCaracteresExtraidos,
        Instant dataUpload
) {
    public static DocumentoResponse de(Documento documento) {
        return new DocumentoResponse(
                documento.getId(),
                documento.getNomeArquivo(),
                documento.getExtensao(),
                documento.getCategoria(),
                documento.getTextoExtraido().length(),
                documento.getDataUpload()
        );
    }
}