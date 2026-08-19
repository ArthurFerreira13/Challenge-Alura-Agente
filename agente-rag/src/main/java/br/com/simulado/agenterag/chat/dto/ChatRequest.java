package br.com.simulado.agenterag.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank String mensagem, String sessindId) {
}
