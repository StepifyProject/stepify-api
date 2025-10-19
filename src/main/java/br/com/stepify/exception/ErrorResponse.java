package br.com.stepify.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String message,
        String details,
        LocalDateTime timestamp
) {
}
