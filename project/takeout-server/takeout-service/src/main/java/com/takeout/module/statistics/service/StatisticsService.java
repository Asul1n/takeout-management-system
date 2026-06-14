package com.takeout.module.statistics.service;

import com.takeout.module.statistics.vo.OrderStatsVO;
import com.takeout.module.statistics.vo.OverviewVO;
import com.takeout.module.statistics.vo.RevenueVO;
import com.takeout.module.statistics.vo.RiderStatsVO;

import java.util.List;

public interface StatisticsService {

    /** 管理员：运营总览 */
    OverviewVO overview();

    /** 管理员：订单统计 */
    OrderStatsVO orderStats(String period);

    /** 管理员：营收统计 */
    RevenueVO revenueStats(String period);

    /** 管理员：骑手绩效 */
    List<RiderStatsVO> riderStats();

    /** 商家：订单趋势 */
    OrderStatsVO merchantOrderStats(Long merchantId, String period);

    /** 商家：营收统计 */
    RevenueVO merchantRevenueStats(Long merchantId, String period);

    /** 骑手：个人配送统计 */
    Long riderDeliveryCount(Long riderId, String period);
}
