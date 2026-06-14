package com.takeout.module.merchant.controller;

import com.takeout.common.result.Result;
import com.takeout.framework.security.UserDetailsImpl;
import com.takeout.module.merchant.dto.MerchantApplyDTO;
import com.takeout.module.merchant.dto.MerchantUpdateDTO;
import com.takeout.module.merchant.service.MerchantService;
import com.takeout.module.merchant.vo.MerchantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "商家接口")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @Operation(summary = "商家入驻申请")
    @PostMapping("/merchant/apply")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Void> apply(@AuthenticationPrincipal UserDetailsImpl userDetails,
                               @Valid @RequestBody MerchantApplyDTO dto) {
        merchantService.apply(userDetails.getUserId(), dto);
        return Result.ok();
    }

    @Operation(summary = "商家修改自身信息")
    @PutMapping("/merchant/me")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<MerchantVO> updateInfo(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @RequestBody MerchantUpdateDTO dto) {
        return Result.ok(merchantService.updateInfo(userDetails.getUserId(), dto));
    }

    @Operation(summary = "商家切换营业状态")
    @PutMapping("/merchant/me/biz-status")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<Void> toggleBizStatus(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestBody Map<String, String> body) {
        merchantService.toggleBizStatus(userDetails.getUserId(), body.get("bizStatus"));
        return Result.ok();
    }

    @Operation(summary = "浏览商家列表")
    @GetMapping("/merchants")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<MerchantVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(merchantService.list(keyword, page, size));
    }
}
