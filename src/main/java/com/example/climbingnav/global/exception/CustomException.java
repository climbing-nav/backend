package com.example.climbingnav.global.exception;

import com.example.climbingnav.global.base.types.ResponseCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{

    private final ResponseCode code;

    public CustomException(ResponseCode code, String message) {
        super(message);
        this.code = code;
    }
}
