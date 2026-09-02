package br.com.agenterag.domain.exception;

public class QuestaoNaoEncontradaException extends RuntimeException {
    public QuestaoNaoEncontradaException(Long questaoId) {
        super("Questão não encontrada: " + questaoId);
    }
}