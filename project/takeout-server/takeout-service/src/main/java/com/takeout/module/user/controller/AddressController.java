package com.takeout.module.user.controller;

import com.takeout.common.result.Result;
import com.takeout.framework.security.UserDetailsImpl;
import com.takeout.module.user.dto.AddressDTO;
import com.takeout.module.user.service.AddressService;
import com.takeout.module.user.vo.AddressVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "配送地址接口")
@RestController
@RequestMapping("/api/v1/customer/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "获取地址列表")
    @GetMapping
    public Result<List<AddressVO>> list(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return Result.ok(addressService.list(userDetails.getUserId()));
    }

    @Operation(summary = "添加地址")
    @PostMapping
    public Result<AddressVO> add(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                  @Valid @RequestBody AddressDTO dto) {
        return Result.ok(addressService.add(userDetails.getUserId(), dto));
    }

    @Operation(summary = "修改地址")
    @PutMapping("/{id}")
    public Result<AddressVO> update(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                     @PathVariable Long id,
                                     @Valid @RequestBody AddressDTO dto) {
        return Result.ok(addressService.update(userDetails.getUserId(), id, dto));
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                @PathVariable Long id) {
        addressService.delete(userDetails.getUserId(), id);
        return Result.ok();
    }

    @Operation(summary = "设为默认地址")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @PathVariable Long id) {
        addressService.setDefault(userDetails.getUserId(), id);
        return Result.ok();
    }
}
