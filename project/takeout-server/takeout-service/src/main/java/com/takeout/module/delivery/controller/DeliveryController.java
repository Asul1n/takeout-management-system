package com.takeout.module.delivery.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.common.result.Result;
import com.takeout.framework.security.UserDetailsImpl;
import com.takeout.module.delivery.service.DeliveryService;
import com.takeout.module.delivery.vo.DeliveryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "配送接口")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @Operation(summary = "骑手：待接配送任务")
    @GetMapping("/rider/tasks")
    @PreAuthorize("hasRole('RIDER')")
    public Result<Page<DeliveryVO>> pendingTasks(@RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(deliveryService.pendingTasks(page, size));
    }

    @Operation(summary = "骑手：接单")
    @PutMapping("/rider/tasks/{deliveryId}/accept")
    @PreAuthorize("hasRole('RIDER')")
    public Result<Void> accept(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                @PathVariable Long deliveryId) {
        deliveryService.acceptDelivery(userDetails.getUserId(), deliveryId);
        return Result.ok();
    }

    @Operation(summary = "骑手：确认取餐")
    @PutMapping("/rider/tasks/{deliveryId}/pickup")
    @PreAuthorize("hasRole('RIDER')")
    public Result<Void> pickup(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                @PathVariable Long deliveryId) {
        deliveryService.pickup(userDetails.getUserId(), deliveryId);
        return Result.ok();
    }

    @Operation(summary = "骑手：确认送达")
    @PutMapping("/rider/tasks/{deliveryId}/deliver")
    @PreAuthorize("hasRole('RIDER')")
    public Result<Void> deliver(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable Long deliveryId) {
        deliveryService.deliver(userDetails.getUserId(), deliveryId);
        return Result.ok();
    }

    @Operation(summary = "骑手：配送记录")
    @GetMapping("/rider/deliveries")
    @PreAuthorize("hasRole('RIDER')")
    public Result<Page<DeliveryVO>> riderDeliveries(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(deliveryService.riderDeliveries(userDetails.getUserId(), page, size));
    }

    @Operation(summary = "管理员：全部配送记录")
    @GetMapping("/admin/deliveries")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<DeliveryVO>> allDeliveries(@RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(deliveryService.allDeliveries(page, size));
    }
}
