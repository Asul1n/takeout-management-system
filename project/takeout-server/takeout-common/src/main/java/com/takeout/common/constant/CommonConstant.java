package com.takeout.common.constant;

/**
 * 公共常量
 */
public interface CommonConstant {

    /** 成功码 */
    int SUCCESS_CODE = 200;

    /** 成功消息 */
    String SUCCESS_MSG = "success";

    /** 参数校验失败 */
    int PARAM_ERROR = 40001;

    /** Token 过期 */
    int TOKEN_EXPIRED = 40101;

    /** Token 无效 */
    int TOKEN_INVALID = 40102;

    /** 无操作权限 */
    int FORBIDDEN = 40301;

    /** 资源不存在 */
    int NOT_FOUND = 40401;

    /** 手机号已注册 */
    int PHONE_EXISTS = 41001;

    /** 密码错误 */
    int PASSWORD_ERROR = 41002;

    /** 账号被禁用 */
    int ACCOUNT_DISABLED = 41003;

    /** 库存不足 */
    int STOCK_INSUFFICIENT = 42001;

    /** 菜品已下架 */
    int DISH_OFF_SHELF = 42002;

    /** 订单状态不允许此操作 */
    int ORDER_STATUS_ERROR = 42003;

    /** 配送地址不完整 */
    int ADDRESS_INCOMPLETE = 42004;

    /** 服务器内部错误 */
    int SERVER_ERROR = 50001;

    /** JWT Token 前缀 */
    String TOKEN_PREFIX = "Bearer ";

    /** JWT 黑名单 Redis Key 前缀 */
    String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    /** 订单编号 Redis Key 前缀 */
    String ORDER_NO_PREFIX = "order:no:";

    /** 骑手锁 Redis Key 前缀 */
    String RIDER_LOCK_PREFIX = "rider:lock:";
}
