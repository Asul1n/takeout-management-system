package com.takeout.module.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.takeout.framework.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_info")
public class Order extends BaseEntity {

    /** 订单编号: YYYYMMDD + 6位流水 */
    @TableId
    private String orderNo;

    /** 顾客ID */
    private Long customerId;

    /** 商家ID */
    private Long merchantId;

    /** 订单状态 */
    private String status;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 备注 */
    private String remark;

    /** 配送地址ID */
    private Long addressId;

    /** 支付方式（预留） */
    private String paymentMethod;

    /** 支付状态（预留） */
    private String paymentStatus;

    /** 支付时间（预留） */
    private LocalDateTime paymentTime;
}
