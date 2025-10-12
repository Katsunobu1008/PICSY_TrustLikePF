package com.picsy.trustlikepf.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    static record ErrorBody(String code, String message) {}

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorBody> handleIllegalState(IllegalStateException ex){
        String code = ex.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if ("SELF_LIKE_NOT_ALLOWED".equals(code)) {
            status = HttpStatus.FORBIDDEN;
        } else if ("INSUFFICIENT_PURCHASING_POWER".equals(code)) {
            status = HttpStatus.PRECONDITION_FAILED;
        }
        return ResponseEntity.status(status).body(new ErrorBody(code, code));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorBody> handleIllegalArg(IllegalArgumentException ex){
        return ResponseEntity.badRequest().body(new ErrorBody("BAD_REQUEST", ex.getMessage()));
    }
}
