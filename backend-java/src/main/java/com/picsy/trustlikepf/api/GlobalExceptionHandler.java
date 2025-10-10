// backend-java/src/main/java/com/picsy/trustlikepf/api/GlobalExceptionHandler.java
package com.picsy.trustlikepf.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String,Object>> handleState(IllegalStateException ex){
        String code = ex.getMessage();
        if (code == null) code = "BAD_REQUEST";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("ok", false, "error", code));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,Object>> handleArg(IllegalArgumentException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("ok", false, "error", ex.getMessage()));
    }
}
