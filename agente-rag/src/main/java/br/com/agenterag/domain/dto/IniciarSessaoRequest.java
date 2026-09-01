package br.com.agenterag.domain.dto;

import jakarta.validation.constraints.NotNull;

public record IniciarSessaoRequest(
        @NotNull(message = "usuarioId é obrigatório")
        Long usuarioId
) {
}