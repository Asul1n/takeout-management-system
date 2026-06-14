package com.takeout.module.statistics.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class RevenueVO {

    /** 总营收 */
    private BigDecimal totalRevenue;

    /** 日均营收 */
    private BigDecimal avgDailyRevenue;

    /** 每日趋势 */
    private List<Map<String, Object>> dailyTrend;
}
