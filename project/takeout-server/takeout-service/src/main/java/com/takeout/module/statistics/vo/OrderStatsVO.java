package com.takeout.module.statistics.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class OrderStatsVO {

    /** 总订单数 */
    private Long totalOrders;

    /** 完成订单数 */
    private Long completedOrders;

    /** 取消订单数 */
    private Long cancelledOrders;

    /** 完成率 */
    private String completionRate;

    /** 每日统计 */
    private List<Map<String, Object>> dailyStats;
}
