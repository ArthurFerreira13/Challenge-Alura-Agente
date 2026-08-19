package br.com.agenterag.ingestion.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record IngestaoRequest(
        @NotNull(message = "O arquivo é obrigatório")
        MultipartFile arquivo,
        String categoria
) {
}
