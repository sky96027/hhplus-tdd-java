package io.hhplus.tdd.common;

import io.hhplus.tdd.common.DTO.ErrorResponse;
import io.hhplus.tdd.common.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 이 클래스는 HTTP 요청을 처리하는 도중 발생한 예외를 처리한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherExceptions(Exception e) {
        System.out.println("처리되지 않은 예외: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "에러가 발생했습니다.");
    }

    // 공통 응답 포맷 생성기
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(String.valueOf(status.value()), message));
    }
}
