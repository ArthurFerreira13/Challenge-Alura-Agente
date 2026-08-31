package br.com.agenterag.domain.exception;

public class SimuladoNaoEncontradoException extends RuntimeException {
    public SimuladoNaoEncontradoException(Long simuladoId) {
        super("Simulado não encontrado: " + simuladoId);
    }}
