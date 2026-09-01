package br.com.agenterag.domain.exception;

public class SessaoExpiradaException extends RuntimeException {
    public SessaoExpiradaException(Long sessaoId) {
        super("Tempo limite da sessão esgotado: " + sessaoId);
    }
}
