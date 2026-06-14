package com.takeout.module.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.takeout.module.delivery.entity.Rider;
import com.takeout.module.delivery.mapper.DeliveryMapper;
import com.takeout.module.delivery.mapper.RiderMapper;
import com.takeout.module.merchant.mapper.MerchantMapper;
import com.takeout.module.order.entity.Order;
import com.takeout.module.order.mapper.OrderMapper;
import com.takeout.module.statistics.service.StatisticsService;
import com.takeout.module.statistics.vo.OrderStatsVO;
import com.takeout.module.statistics.vo.OverviewVO;
import com.takeout.module.statistics.vo.RevenueVO;
import com.takeout.module.statistics.vo.RiderStatsVO;
import com.takeout.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final RiderMapper riderMapper;
    private final DeliveryMapper deliveryMapper;

    @Override
    public OverviewVO overview() {
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.now().with(LocalTime.MAX);

        OverviewVO vo = new OverviewVO();

        vo.setTodayOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().between(Order::getCreateTime, todayStart, todayEnd)));
        vo.setTodayRevenue(orderMapper.totalRevenue(todayStart, todayEnd));
        vo.setTotalUsers(userMapper.selectCount(null));
        vo.setTotalMerchants(merchantMapper.selectCount(
                new LambdaQueryWrapper<com.takeout.module.merchant.entity.Merchant>()
                        .eq(com.takeout.module.merchant.entity.Merchant::getAuditStatus, "已通过")));
        vo.setDeliveringOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, "配送中")));

        return vo;
    }

    @Override
    public OrderStatsVO orderStats(String period) {
        LocalDateTime start = getStartDate(period);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        Long total = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().between(Order::getCreateTime, start, end));
        Long completed = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().between(Order::getCreateTime, start, end)
                        .eq(Order::getStatus, "已完成"));
        Long cancelled = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().between(Order::getCreateTime, start, end)
                        .eq(Order::getStatus, "已取消"));

        BigDecimal rate = total > 0
                ? BigDecimal.valueOf(completed).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return OrderStatsVO.builder()
                .totalOrders(total)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .completionRate(rate + "%")
                .dailyStats(orderMapper.countByDateRange(start, end))
                .build();
    }

    @Override
    public RevenueVO revenueStats(String period) {
        LocalDateTime start = getStartDate(period);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        BigDecimal total = orderMapper.totalRevenue(start, end);
        long days = Math.max(1, java.time.Duration.between(start, end).toDays());
        BigDecimal avg = total.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);

        return RevenueVO.builder()
                .totalRevenue(total)
                .avgDailyRevenue(avg)
                .dailyTrend(orderMapper.revenueByDateRange(start, end))
                .build();
    }

    @Override
    public List<RiderStatsVO> riderStats() {
        List<Rider> riders = riderMapper.selectList(null);
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.now().with(LocalTime.MAX);

        return riders.stream().map(r -> {
            RiderStatsVO vo = new RiderStatsVO();
            vo.setRiderId(r.getId());
            vo.setRiderName(r.getName());
            vo.setStatus(r.getStatus());
            vo.setTotalDeliveries((long) r.getTotalDeliveries());

            Long todayCount = deliveryMapper.selectCount(
                    new LambdaQueryWrapper<com.takeout.module.delivery.entity.Delivery>()
                            .eq(com.takeout.module.delivery.entity.Delivery::getRiderId, r.getId())
                            .between(com.takeout.module.delivery.entity.Delivery::getCreateTime, todayStart, todayEnd));
            vo.setTodayDeliveries(todayCount);

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public OrderStatsVO merchantOrderStats(Long merchantId, String period) {
        LocalDateTime start = getStartDate(period);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId)
                .between(Order::getCreateTime, start, end);

        Long total = orderMapper.selectCount(wrapper);
        Long completed = orderMapper.selectCount(wrapper.clone().eq(Order::getStatus, "已完成"));
        Long cancelled = orderMapper.selectCount(wrapper.clone().eq(Order::getStatus, "已取消"));

        return OrderStatsVO.builder()
                .totalOrders(total)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .build();
    }

    @Override
    public RevenueVO merchantRevenueStats(Long merchantId, String period) {
        LocalDateTime start = getStartDate(period);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId)
                .eq(Order::getStatus, "已完成")
                .between(Order::getCreateTime, start, end);

        List<Order> orders = orderMapper.selectList(wrapper);
        BigDecimal total = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RevenueVO.builder().totalRevenue(total).build();
    }

    @Override
    public Long riderDeliveryCount(Long riderId, String period) {
        LocalDateTime start = getStartDate(period);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        return deliveryMapper.selectCount(
                new LambdaQueryWrapper<com.takeout.module.delivery.entity.Delivery>()
                        .eq(com.takeout.module.delivery.entity.Delivery::getRiderId, riderId)
                        .between(com.takeout.module.delivery.entity.Delivery::getCreateTime, start, end));
    }

    private LocalDateTime getStartDate(String period) {
        return switch (period != null ? period : "week") {
            case "today" -> LocalDateTime.now().with(LocalTime.MIN);
            case "week" -> LocalDateTime.now().minusDays(7).with(LocalTime.MIN);
            case "month" -> LocalDateTime.now().minusDays(30).with(LocalTime.MIN);
            default -> LocalDateTime.now().minusDays(7).with(LocalTime.MIN);
        };
    }
}
