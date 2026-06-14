package com.takeout.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.takeout.module.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /** 统计某商家的已完成订单数（近30天） */
    @Select("SELECT COUNT(*) FROM order_info WHERE merchant_id = #{merchantId} AND status = '已完成' AND create_time >= #{since}")
    int countCompletedByMerchant(@Param("merchantId") Long merchantId, @Param("since") LocalDateTime since);

    /** 按日期范围统计每日订单数 */
    @Select("SELECT DATE(create_time) as date, COUNT(*) as count FROM order_info WHERE create_time BETWEEN #{start} AND #{end} GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 按日期范围统计每日营收 */
    @Select("SELECT DATE(create_time) as date, SUM(total_amount) as revenue FROM order_info WHERE status = '已完成' AND create_time BETWEEN #{start} AND #{end} GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> revenueByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 统计某时间范围内的总营收 */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM order_info WHERE status = '已完成' AND create_time BETWEEN #{start} AND #{end}")
    BigDecimal totalRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 统计各状态的订单数 */
    @Select("SELECT status, COUNT(*) as count FROM order_info GROUP BY status")
    List<Map<String, Object>> countByStatus();
}
