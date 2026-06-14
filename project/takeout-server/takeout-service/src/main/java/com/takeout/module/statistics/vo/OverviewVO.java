package com.takeout.module.statistics.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OverviewVO {

    /** 今日订单数 */
    private Long todayOrders;

    /** 今日营收 */
    private BigDecimal todayRevenue;

    /** 总用户数 */
    private Long totalUsers;

    /** 总商家数 */
    private Long totalMerchants;

    /** 配送中的订单数 */
    private Long deliveringOrders;
}
