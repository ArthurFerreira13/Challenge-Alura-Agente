package br.com.arthur.agenterag.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank String mensagem, String sessindId) {
}
