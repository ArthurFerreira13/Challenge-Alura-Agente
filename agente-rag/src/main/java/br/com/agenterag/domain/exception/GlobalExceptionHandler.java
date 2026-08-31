package br.com.agenterag.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(SimuladoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrado(SimuladoNaoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SessaoNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrado(SessaoNaoEncontradaException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(QuestaoNaoPertenceAoSimuladoException.class)
    public ResponseEntity<ErroResponse> handleConflito(QuestaoNaoPertenceAoSimuladoException ex) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(SessaoJaFinalizadaException.class)
    public ResponseEntity<ErroResponse> handleConflito(SessaoJaFinalizadaException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleInesperado(Exception ex) {
        // nunca devolve ex.getMessage() aqui — mensagem genérica pro cliente,
        // detalhe real vai só pro log (evita vazar stacktrace/detalhe interno)
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado ao processar a solicitação.");
    }

    private ResponseEntity<ErroResponse> responder(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(status.value(), mensagem, LocalDateTime.now(clock)));
    }
}