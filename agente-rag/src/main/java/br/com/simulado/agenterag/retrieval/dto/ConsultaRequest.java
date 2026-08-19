package br.com.simulado.agenterag.retrieval.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsultaRequest(@NotBlank String pergunta) {
}
