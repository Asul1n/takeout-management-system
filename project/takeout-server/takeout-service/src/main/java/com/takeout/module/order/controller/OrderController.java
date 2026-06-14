package com.takeout.module.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.common.result.Result;
import com.takeout.framework.security.UserDetailsImpl;
import com.takeout.module.order.dto.OrderQueryDTO;
import com.takeout.module.order.dto.OrderSubmitDTO;
import com.takeout.module.order.service.OrderService;
import com.takeout.module.order.vo.OrderDetailVO;
import com.takeout.module.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单接口")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "顾客提交订单")
    @PostMapping("/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<OrderVO> submit(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                   @Valid @RequestBody OrderSubmitDTO dto) {
        return Result.ok(orderService.submitOrder(userDetails.getUserId(), dto));
    }

    @Operation(summary = "订单详情（仅顾客本人/对应商家/管理员可查看）")
    @GetMapping("/orders/{orderNo}")
    public Result<OrderDetailVO> detail(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @PathVariable String orderNo) {
        return Result.ok(orderService.detail(userDetails.getUserId(), userDetails.getRole(), orderNo));
    }

    @Operation(summary = "顾客：我的订单")
    @GetMapping("/customer/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Page<OrderVO>> customerOrders(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                OrderQueryDTO dto) {
        return Result.ok(orderService.listByCustomer(userDetails.getUserId(), dto));
    }

    @Operation(summary = "商家：本店订单")
    @GetMapping("/merchant/orders")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Page<OrderVO>> merchantOrders(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                OrderQueryDTO dto) {
        return Result.ok(orderService.listByMerchant(userDetails.getUserId(), dto));
    }

    @Operation(summary = "管理员：全平台订单")
    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<OrderVO>> adminOrders(OrderQueryDTO dto) {
        return Result.ok(orderService.listAll(dto));
    }

    @Operation(summary = "商家：接单")
    @PutMapping("/merchant/orders/{orderNo}/accept")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Void> accept(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                @PathVariable String orderNo) {
        orderService.acceptOrder(userDetails.getUserId(), orderNo);
        return Result.ok();
    }

    @Operation(summary = "商家：备餐完成")
    @PutMapping("/merchant/orders/{orderNo}/prepare")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Void> prepareComplete(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @PathVariable String orderNo) {
        orderService.prepareComplete(userDetails.getUserId(), orderNo);
        return Result.ok();
    }

    @Operation(summary = "顾客：取消订单（接单前直接取消）")
    @PutMapping("/customer/orders/{orderNo}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Void> cancel(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                @PathVariable String orderNo) {
        orderService.cancelOrder(userDetails.getUserId(), orderNo);
        return Result.ok();
    }

    @Operation(summary = "顾客：申请取消（接单后需商家同意）")
    @PutMapping("/customer/orders/{orderNo}/request-cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Void> requestCancel(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                       @PathVariable String orderNo) {
        orderService.requestCancel(userDetails.getUserId(), orderNo);
        return Result.ok();
    }

    @Operation(summary = "商家：处理取消申请")
    @PutMapping("/merchant/orders/{orderNo}/cancel-review")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Void> merchantCancel(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @PathVariable String orderNo,
                                        @RequestBody Map<String, Boolean> body) {
        orderService.merchantCancel(userDetails.getUserId(), orderNo, body.getOrDefault("approved", false));
        return Result.ok();
    }

    @Operation(summary = "顾客：确认收货")
    @PutMapping("/customer/orders/{orderNo}/confirm")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Void> confirm(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable String orderNo) {
        orderService.confirmReceipt(userDetails.getUserId(), orderNo);
        return Result.ok();
    }
}
