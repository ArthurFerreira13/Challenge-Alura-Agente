package br.com.agenterag.domain.dto;

import br.com.agenterag.domain.internal.Disciplina;
import br.com.agenterag.domain.internal.Questao;

import java.util.List;

public record QuestaoResponse(
        Long questaoId,
        int numeroQuestao,
        String enunciado,
        Disciplina disciplina,
        List<AlternativaResponse> alternativas
) {
    public static QuestaoResponse from(Questao questao) {
        return new QuestaoResponse(
                questao.getId(),
                questao.getNumeroQuestao(),
                questao.getEnunciado(),
                questao.getDisciplina(),
                questao.getAlternativas().stream()
                        .map(AlternativaResponse::from)
                        .toList()
        );
    }
}