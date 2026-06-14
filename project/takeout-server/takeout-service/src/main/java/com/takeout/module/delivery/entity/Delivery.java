package com.takeout.module.delivery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.takeout.framework.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 配送信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("delivery")
public class Delivery extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 骑手ID */
    private Long riderId;

    /** 配送状态: 待取餐/配送中/已送达 */
    private String status;

    /** 取餐时间 */
    private LocalDateTime pickupTime;

    /** 送达时间 */
    private LocalDateTime deliverTime;
}
