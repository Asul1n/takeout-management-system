package com.takeout.common.exception;

import com.takeout.common.constant.CommonConstant;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = CommonConstant.SERVER_ERROR;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
