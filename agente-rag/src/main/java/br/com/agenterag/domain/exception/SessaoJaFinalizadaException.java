package br.com.agenterag.domain.exception;

public class SessaoJaFinalizadaException extends RuntimeException {
    public SessaoJaFinalizadaException(Long sessaoId) {
        super("Sessão já finalizada ou abandonada: " + sessaoId);
    }
}
