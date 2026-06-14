package com.takeout.module.dish.controller;

import com.takeout.common.result.Result;
import com.takeout.framework.security.UserDetailsImpl;
import com.takeout.module.dish.dto.CategorySaveDTO;
import com.takeout.module.dish.service.CategoryService;
import com.takeout.module.dish.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜品分类接口")
@RestController
@RequestMapping("/api/v1/merchant/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "商家查看自己的分类")
    @GetMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<List<CategoryVO>> list(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return Result.ok(categoryService.listByMerchant(userDetails.getUserId()));
    }

    @Operation(summary = "按商家ID查看分类（顾客/管理员可用）")
    @GetMapping("/by-merchant")
    public Result<List<CategoryVO>> listByMerchantId(@RequestParam Long merchantId) {
        return Result.ok(categoryService.listByMerchant(merchantId));
    }

    @PostMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<CategoryVO> add(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                   @Valid @RequestBody CategorySaveDTO dto) {
        return Result.ok(categoryService.add(userDetails.getUserId(), dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<CategoryVO> update(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                      @PathVariable Long id,
                                      @Valid @RequestBody CategorySaveDTO dto) {
        return Result.ok(categoryService.update(userDetails.getUserId(), id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Void> delete(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                @PathVariable Long id) {
        categoryService.delete(userDetails.getUserId(), id);
        return Result.ok();
    }
}
