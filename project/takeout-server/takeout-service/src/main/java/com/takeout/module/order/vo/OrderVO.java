package com.takeout.module.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {

    private String orderNo;
    private Long customerId;
    private String customerName;
    private Long merchantId;
    private String merchantName;
    private Long addressId;
    private String address;
    private String status;
    private BigDecimal totalAmount;
    private String remark;
    private LocalDateTime createTime;
    private List<OrderItemVO> items;
}
