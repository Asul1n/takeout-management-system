package com.takeout.module.dish.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.common.result.Result;
import com.takeout.framework.security.UserDetailsImpl;
import com.takeout.module.dish.dto.DishQueryDTO;
import com.takeout.module.dish.dto.DishSaveDTO;
import com.takeout.module.dish.service.DishService;
import com.takeout.module.dish.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "菜品接口")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @Operation(summary = "搜索/浏览菜品")
    @GetMapping("/dishes")
    public Result<Page<DishVO>> list(DishQueryDTO dto) {
        return Result.ok(dishService.list(dto));
    }

    @Operation(summary = "菜品详情")
    @GetMapping("/dishes/{id}")
    public Result<DishVO> detail(@PathVariable Long id) {
        return Result.ok(dishService.detail(id));
    }

    @Operation(summary = "商家添加菜品")
    @PostMapping("/merchant/dishes")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<DishVO> add(@AuthenticationPrincipal UserDetailsImpl userDetails,
                               @Valid @RequestBody DishSaveDTO dto) {
        return Result.ok(dishService.add(userDetails.getUserId(), dto));
    }

    @Operation(summary = "商家修改菜品")
    @PutMapping("/merchant/dishes/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<DishVO> update(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                  @PathVariable Long id,
                                  @Valid @RequestBody DishSaveDTO dto) {
        return Result.ok(dishService.update(userDetails.getUserId(), id, dto));
    }

    @Operation(summary = "商家上下架菜品")
    @PutMapping("/merchant/dishes/{id}/status")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Void> toggleStatus(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                      @PathVariable Long id,
                                      @RequestBody Map<String, String> body) {
        dishService.toggleStatus(userDetails.getUserId(), id, body.get("status"));
        return Result.ok();
    }

    @Operation(summary = "商家调整库存")
    @PutMapping("/merchant/dishes/{id}/stock")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Void> updateStock(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                     @PathVariable Long id,
                                     @RequestBody Map<String, Integer> body) {
        dishService.updateStock(userDetails.getUserId(), id, body.get("stock"));
        return Result.ok();
    }
}
