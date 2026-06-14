package com.takeout.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

/**
 * 订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    SUBMITTED("已提交", "顾客已提交订单"),
    PENDING("待接单", "等待商家接单"),
    PREPARING("备餐中", "商家已接单，备餐中"),
    WAITING_DELIVERY("待配送", "备餐完成，等待骑手配送"),
    DELIVERING("配送中", "骑手配送中"),
    DELIVERED("已送达", "骑手已送达"),
    COMPLETED("已完成", "顾客确认收货，订单完成"),
    CANCELLED("已取消", "订单已取消");

    private final String code;
    private final String desc;

    /**
     * 状态允许的下一状态
     */
    public static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
        "已提交", Set.of("待接单", "备餐中", "已取消"),
        "待接单", Set.of("备餐中", "已取消"),
        "备餐中", Set.of("待配送"),
        "待配送", Set.of("配送中"),
        "配送中", Set.of("已送达"),
        "已送达", Set.of("已完成")
    );

    public static boolean canTransition(String from, String to) {
        Set<String> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }
}
