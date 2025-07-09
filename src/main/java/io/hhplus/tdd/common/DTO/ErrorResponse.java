package io.hhplus.tdd.common.DTO;

import java.time.LocalDateTime;

public class ErrorResponse {
    private final String error;
    private final String message;
    private final LocalDateTime timestamp;

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
