package com.takeout.module.statistics.controller;

import com.takeout.common.result.Result;
import com.takeout.framework.security.UserDetailsImpl;
import com.takeout.module.statistics.service.StatisticsService;
import com.takeout.module.statistics.vo.OrderStatsVO;
import com.takeout.module.statistics.vo.OverviewVO;
import com.takeout.module.statistics.vo.RevenueVO;
import com.takeout.module.statistics.vo.RiderStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "统计接口")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "管理员：运营总览")
    @GetMapping("/admin/statistics/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<OverviewVO> overview() {
        return Result.ok(statisticsService.overview());
    }

    @Operation(summary = "管理员：订单统计")
    @GetMapping("/admin/statistics/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<OrderStatsVO> orderStats(@RequestParam(defaultValue = "week") String period) {
        return Result.ok(statisticsService.orderStats(period));
    }

    @Operation(summary = "管理员：营收统计")
    @GetMapping("/admin/statistics/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<RevenueVO> revenueStats(@RequestParam(defaultValue = "week") String period) {
        return Result.ok(statisticsService.revenueStats(period));
    }

    @Operation(summary = "管理员：骑手绩效")
    @GetMapping("/admin/statistics/riders")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<RiderStatsVO>> riderStats() {
        return Result.ok(statisticsService.riderStats());
    }

    @Operation(summary = "商家：订单趋势")
    @GetMapping("/merchant/statistics/orders")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<OrderStatsVO> merchantOrderStats(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    @RequestParam(defaultValue = "week") String period) {
        return Result.ok(statisticsService.merchantOrderStats(userDetails.getUserId(), period));
    }

    @Operation(summary = "商家：营收统计")
    @GetMapping("/merchant/statistics/revenue")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<RevenueVO> merchantRevenueStats(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                   @RequestParam(defaultValue = "week") String period) {
        return Result.ok(statisticsService.merchantRevenueStats(userDetails.getUserId(), period));
    }

    @Operation(summary = "骑手：个人配送统计")
    @GetMapping("/rider/statistics")
    @PreAuthorize("hasRole('RIDER')")
    public Result<Long> riderDeliveryCount(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @RequestParam(defaultValue = "week") String period) {
        return Result.ok(statisticsService.riderDeliveryCount(userDetails.getUserId(), period));
    }
}
