package com.example.climbingnav.global.base;

import com.example.climbingnav.global.base.types.ResponseCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"code", "message", "data"})
public record ApiResponse<T>(
        ResponseCode code,
        String message,
        T data
) {
    public static <T> ApiResponse<T> ok(T data) {
        return ok(ResponseCode.OK, "success", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ok(ResponseCode.OK, message, data);
    }

    public static <T> ApiResponse<T> error(ResponseCode code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    private static <T> ApiResponse<T> ok(ResponseCode code,
                                         String message,
                                         T data)
    {
        return new ApiResponse<>(
                code,
                message,
                data
        );
    }
}