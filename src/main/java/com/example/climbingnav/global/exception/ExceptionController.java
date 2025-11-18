package com.example.climbingnav.global.exception;

import com.example.climbingnav.global.base.ApiResponse;
import com.example.climbingnav.global.base.types.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.BindException;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException exception) {
        ResponseCode code = exception.getCode();

        log.warn("[CustomException] code={}, message={}", code, exception.getMessage());

        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.error(code, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        log.warn("[Validation Exception] {}", exception.getMessage());

        return ResponseEntity
                .status(ResponseCode.VALIDATION_ERROR.httpStatus())
                .body(ApiResponse.error(ResponseCode.VALIDATION_ERROR, exception.getMessage()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
        log.warn("[BindException] {}", exception.getMessage());

        return ResponseEntity
                .status(ResponseCode.BAD_REQUEST.httpStatus())
                .body(ApiResponse.error(ResponseCode.BAD_REQUEST, exception.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        String message = "지원하지 않는 HTTP 메서드입니다. method=" + exception.getMethod();

        log.warn("[MethodNotSupported] {}", message);

        return ResponseEntity
                .status(ResponseCode.BAD_REQUEST.httpStatus())
                .body(ApiResponse.error(ResponseCode.BAD_REQUEST, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.error("[Unhandled Exception]", exception);

        String message = "서버 내부 오류가 발생했습니다.";

        return ResponseEntity
                .status(ResponseCode.INTERNAL_ERROR.httpStatus())
                .body(ApiResponse.error(ResponseCode.INTERNAL_ERROR, message));
    }
}
