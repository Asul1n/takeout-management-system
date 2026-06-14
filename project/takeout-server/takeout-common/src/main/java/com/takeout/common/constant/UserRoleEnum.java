package com.takeout.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户身份枚举
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    ADMIN("ADMIN", "管理员"),
    MERCHANT("MERCHANT", "商家"),
    CUSTOMER("CUSTOMER", "顾客"),
    RIDER("RIDER", "骑手");

    private final String code;
    private final String desc;

    public static boolean isValid(String code) {
        for (UserRoleEnum role : values()) {
            if (role.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
