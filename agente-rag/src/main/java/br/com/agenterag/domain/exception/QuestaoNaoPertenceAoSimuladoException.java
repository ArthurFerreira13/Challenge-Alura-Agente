package br.com.agenterag.domain.exception;

public class QuestaoNaoPertenceAoSimuladoException extends RuntimeException {
    public QuestaoNaoPertenceAoSimuladoException(Long questaoId, String simuladoId) {
        super("Questão " + questaoId + " não pertence ao simulado " + simuladoId);
    }
}