package com.takeout.module.merchant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.common.result.Result;
import com.takeout.module.merchant.service.MerchantService;
import com.takeout.module.merchant.vo.MerchantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "管理员 - 商家管理")
@RestController
@RequestMapping("/api/v1/admin/merchants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMerchantController {

    private final MerchantService merchantService;

    @Operation(summary = "待审核商家列表")
    @GetMapping("/audit")
    public Result<Page<MerchantVO>> auditList(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(merchantService.auditList(page, size));
    }

    @Operation(summary = "审核商家")
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, String> body) {
        merchantService.audit(id, body.get("auditStatus"), body.getOrDefault("reason", ""));
        return Result.ok();
    }
}
