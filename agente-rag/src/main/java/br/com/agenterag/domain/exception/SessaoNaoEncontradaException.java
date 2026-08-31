package br.com.agenterag.domain.exception;

public class SessaoNaoEncontradaException extends RuntimeException {
    public SessaoNaoEncontradaException(Long sessaoId) {
        super("Sessão não encontrada: " + sessaoId);
    }
}
