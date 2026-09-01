package br.com.agenterag.domain.dto;

import br.com.agenterag.domain.internal.Simulado;

public record SimuladoResumoResponse(
        Long id,
        String titulo,
        Integer tempoLimiteMinutos,
        int totalQuestoes
) {
    public static SimuladoResumoResponse from(Simulado simulado) {
        return new SimuladoResumoResponse(
                simulado.getId(),
                simulado.getTitulo(),
                simulado.getTempoLimiteMinutos(),
                simulado.getQuestoes().size()
        );
    }
}