package br.com.agenterag.domain.dto;

import br.com.agenterag.domain.internal.DesempenhoPorDisciplina;
import br.com.agenterag.domain.internal.Disciplina;

public record DesempenhoDisciplinaResponse(
        Disciplina disciplina,
        String descricao,
        int acertos,
        int total
) {
    public static DesempenhoDisciplinaResponse from(DesempenhoPorDisciplina desempenho) {
        return new DesempenhoDisciplinaResponse(
                desempenho.getDisciplina(),
                desempenho.getDisciplina().getDescricao(),
                desempenho.getAcertos(),
                desempenho.getTotal()
        );
    }
}