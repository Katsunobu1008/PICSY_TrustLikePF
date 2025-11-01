// backend-java/src/main/java/com/picsy/trustlikepf/api/GlobalExceptionHandler.java
package com.picsy.trustlikepf.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public static record ErrorBody(String code, String message) {} // ★ public に

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorBody> handleIllegalState(IllegalStateException ex){
        String code = ex.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if ("SELF_LIKE_NOT_ALLOWED".equals(code)) {
            status = HttpStatus.FORBIDDEN;
        } else if ("INSUFFICIENT_PURCHASING_POWER".equals(code)) {
            status = HttpStatus.PRECONDITION_FAILED;
        } else if ("ACCOUNT_FROZEN".equals(code) || "TARGET_FROZEN".equals(code)) { // ★ 追加
            status = HttpStatus.FORBIDDEN;
        } else if ("INVALID_DEFAULT_BETA".equals(code)
                || "ORIGINAL_ROYALTY_NOT_SET".equals(code)
                || "INVALID_ROYALTY_RATE".equals(code)
                || "ORIGINAL_POST_ID_MISSING".equals(code)) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ResponseEntity.status(status).body(new ErrorBody(code, code));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorBody> handleIllegalArg(IllegalArgumentException ex){
        return ResponseEntity.badRequest().body(new ErrorBody("BAD_REQUEST", ex.getMessage()));
    }
}
