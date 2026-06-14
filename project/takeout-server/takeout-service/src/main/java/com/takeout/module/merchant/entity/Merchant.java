package com.takeout.module.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.takeout.framework.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商家信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant")
public class Merchant extends BaseEntity {

    /** 商家ID = 用户ID */
    @TableId
    private Long id;

    /** 商家名称 */
    private String name;

    /** 省 */
    private String province;

    /** 市 */
    private String city;

    /** 区 */
    private String district;

    /** 详细地址 */
    private String addressDetail;

    /** 营业开始时间 */
    private String openTime;

    /** 营业结束时间 */
    private String closeTime;

    /** 商家公告 */
    private String notice;

    /** 营业状态: 营业中/休息中 */
    private String bizStatus;

    /** 审核状态: 待审核/已通过/已驳回 */
    private String auditStatus;

    /** 月销量 */
    private Integer monthlySales;

    /** 商家综合评分（预留） */
    private BigDecimal rating;

    /** 是否自动接单: 0否/1是 */
    private Integer autoAccept;
}
