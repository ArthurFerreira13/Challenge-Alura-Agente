package br.com.agenterag.domain.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(QuestaoNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrado(QuestaoNaoEncontradaException ex) {
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

    @ExceptionHandler(SessaoExpiradaException.class)
    public ResponseEntity<ErroResponse> handleExpirada(SessaoExpiradaException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> handleCorpoRequisicaoAusenteOuInvalido(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "O corpo da requisição é obrigatório e deve ser um JSON válido.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacaoAtributos(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Dados de requisição inválidos.");
        return responder(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleInesperado(Exception ex) {
        log.error("Erro inesperado processando requisição", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado ao processar a solicitação.");
    }

    @ExceptionHandler(br.com.agenterag.domain.exception.EmailJaCadastradoException.class)
    public ResponseEntity<ErroResponse> handleConflito(br.com.agenterag.domain.exception.EmailJaCadastradoException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(br.com.agenterag.domain.exception.UsuarioNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrado(br.com.agenterag.domain.exception.UsuarioNaoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    private ResponseEntity<ErroResponse> responder(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(status.value(), mensagem, LocalDateTime.now(clock)));
    }
}