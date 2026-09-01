package br.com.agenterag.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ResponderRequest(
        @NotNull(message = "questaoId é obrigatório")
        Long questaoId,

        @NotBlank(message = "alternativa é obrigatória")
        @Pattern(regexp = "[A-Za-z]", message = "alternativa deve ser uma única letra (ex.: A, B, C, D)")
        String alternativa
) {
}