package com.example.climbingnav.global.base.types;

import org.springframework.http.HttpStatus;

public enum ResponseCode {
    OK(true, HttpStatus.OK),
    CREATED(true, HttpStatus.CREATED),
    BAD_REQUEST(false, HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(false, HttpStatus.UNAUTHORIZED),
    FORBIDDEN(false, HttpStatus.FORBIDDEN),
    NOT_FOUND(false, HttpStatus.NOT_FOUND),
    VALIDATION_ERROR(false, HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR);

    private final boolean success;
    private final HttpStatus httpStatus;

    ResponseCode(boolean success, HttpStatus httpStatus) {
        this.success = success;
        this.httpStatus = httpStatus;
    }

    public boolean success() { return success; }
    public HttpStatus httpStatus() { return httpStatus; }
}
