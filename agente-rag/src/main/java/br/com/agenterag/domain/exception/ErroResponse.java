package br.com.agenterag.domain.exception;

import java.time.LocalDateTime;

public record ErroResponse(int status, String mensagem, LocalDateTime timestamp) {
}