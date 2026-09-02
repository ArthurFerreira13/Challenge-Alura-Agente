package br.com.agenterag.domain.dto;

import java.time.LocalDateTime;

public record AnotacaoResponse(
        Long id,
        Long usuarioId,
        Long questaoId,
        String texto,
        LocalDateTime atualizadoEm
) {}
