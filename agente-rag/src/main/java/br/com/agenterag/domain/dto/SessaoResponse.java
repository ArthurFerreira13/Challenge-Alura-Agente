package br.com.agenterag.domain.dto;

import br.com.agenterag.domain.internal.SimuladoSessao;

import java.time.Clock;
import java.time.LocalDateTime;

public record SessaoResponse(
        Long sessaoId,
        Long simuladoId,
        Long usuarioId,
        String status,
        LocalDateTime iniciadoEm,
        long tempoRestanteSegundos
) {
    public static SessaoResponse from(SimuladoSessao sessao, Clock clock) {
        return new SessaoResponse(
                sessao.getId(),
                sessao.getSimuladoId(),
                sessao.getUsuarioId(),
                sessao.getStatus().name(),
                sessao.getIniciadoEm(),
                sessao.getTempoRestanteSegundos(LocalDateTime.now(clock))
        );
    }
}