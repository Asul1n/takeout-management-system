package com.takeout.module.order.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单详情VO — 继承 OrderVO，增加配送信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDetailVO extends OrderVO {

    private String deliveryStatus;
    private String riderName;
    private String riderPhone;
}
