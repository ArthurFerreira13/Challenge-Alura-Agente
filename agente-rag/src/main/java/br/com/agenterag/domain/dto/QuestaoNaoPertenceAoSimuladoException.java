package br.com.agenterag.domain.dto;

public class QuestaoNaoPertenceAoSimuladoException extends RuntimeException {
    public QuestaoNaoPertenceAoSimuladoException(Long questaoId, Long simuladoId) {
        super("Questão " + questaoId + " não pertence ao simulado " + simuladoId);
    }}
