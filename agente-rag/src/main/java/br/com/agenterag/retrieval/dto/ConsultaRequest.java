package br.com.agenterag.retrieval.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsultaRequest(@NotBlank String pergunta) {
}
