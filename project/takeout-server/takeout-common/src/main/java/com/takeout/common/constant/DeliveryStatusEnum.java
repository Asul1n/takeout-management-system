package com.takeout.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配送状态枚举
 */
@Getter
@AllArgsConstructor
public enum DeliveryStatusEnum {

    WAITING_PICKUP("待取餐", "骑手已接单，等待取餐"),
    DELIVERING("配送中", "骑手配送中"),
    DELIVERED("已送达", "骑手已送达");

    private final String code;
    private final String desc;
}
