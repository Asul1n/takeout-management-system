package com.takeout.module.delivery.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeliveryVO {

    private Long id;
    private String orderNo;
    private Long riderId;
    private String riderName;
    private String status;
    private LocalDateTime pickupTime;
    private LocalDateTime deliverTime;
    private LocalDateTime createTime;

    // 订单关联信息
    private String customerAddress;
    private String merchantName;
    private String merchantAddress;
}
