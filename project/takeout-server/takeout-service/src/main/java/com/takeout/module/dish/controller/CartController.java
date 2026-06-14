package com.takeout.module.dish.controller;

import com.takeout.common.result.Result;
import com.takeout.framework.security.UserDetailsImpl;
import com.takeout.module.dish.entity.CartItem;
import com.takeout.module.dish.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "购物车接口")
@RestController
@RequestMapping("/api/v1/customer/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Result<List<CartItem>> list(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return Result.ok(cartService.list(userDetails.getUserId()));
    }

    @PostMapping
    public Result<Void> add(@AuthenticationPrincipal UserDetailsImpl userDetails,
                             @RequestBody Map<String, Object> body) {
        Long dishId = Long.valueOf(body.get("dishId").toString());
        Integer quantity = Integer.valueOf(body.getOrDefault("quantity", 1).toString());
        cartService.add(userDetails.getUserId(), dishId, quantity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> updateQuantity(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @PathVariable Long id,
                                        @RequestBody Map<String, Integer> body) {
        cartService.updateQuantity(userDetails.getUserId(), id, body.get("quantity"));
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                @PathVariable Long id) {
        cartService.remove(userDetails.getUserId(), id);
        return Result.ok();
    }

    @DeleteMapping
    public Result<Void> clear(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        cartService.clear(userDetails.getUserId());
        return Result.ok();
    }
}
