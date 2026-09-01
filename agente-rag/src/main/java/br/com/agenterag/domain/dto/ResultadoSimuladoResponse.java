package br.com.agenterag.domain.dto;

import br.com.agenterag.domain.internal.ResultadoSimulado;

import java.time.LocalDateTime;
import java.util.List;

public record ResultadoSimuladoResponse(
        Long sessaoId,
        int totalAcertos,
        int totalErros,
        int totalEmBranco,
        double percentualAcerto,
        boolean aprovado,
        long tempoGastoSegundos,
        LocalDateTime geradoEm,
        List<DesempenhoDisciplinaResponse> desempenhoPorDisciplina
) {
    public static ResultadoSimuladoResponse from(ResultadoSimulado resultado) {
        return new ResultadoSimuladoResponse(
                resultado.getSessao().getId(),
                resultado.getTotalAcertos(),
                resultado.getTotalErros(),
                resultado.getTotalEmBranco(),
                resultado.getPercentualAcerto(),
                resultado.isAprovado(),
                resultado.getTempoGastoSegundos(),
                resultado.getGeradoEm(),
                resultado.getDesempenhoPorDisciplina().stream()
                        .map(DesempenhoDisciplinaResponse::from)
                        .toList()
        );
    }
}