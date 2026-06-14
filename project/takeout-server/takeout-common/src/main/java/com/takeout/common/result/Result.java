package com.takeout.common.result;

import com.takeout.common.constant.CommonConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok() {
        return new Result<>(CommonConstant.SUCCESS_CODE, CommonConstant.SUCCESS_MSG, null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(CommonConstant.SUCCESS_CODE, CommonConstant.SUCCESS_MSG, data);
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(CommonConstant.SUCCESS_CODE, message, data);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(CommonConstant.SERVER_ERROR, message, null);
    }
}
