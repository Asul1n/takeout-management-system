package com.takeout.module.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.common.result.Result;
import com.takeout.module.user.dto.RegisterDTO;
import com.takeout.module.user.dto.UserQueryDTO;
import com.takeout.module.user.service.UserService;
import com.takeout.module.user.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "管理员 - 用户管理")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "用户列表（分页/筛选）")
    @GetMapping
    public Result<Page<UserInfoVO>> listUsers(UserQueryDTO dto) {
        return Result.ok(userService.listUsers(dto));
    }

    @Operation(summary = "管理员创建用户（商家/骑手/顾客）")
    @PostMapping
    public Result<Void> createUser(@Valid @RequestBody RegisterDTO dto) {
        userService.createUser(dto);
        return Result.ok();
    }

    @Operation(summary = "删除用户及关联数据")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.ok();
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/{id}/status")
    public Result<Void> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.toggleUserStatus(id, body.get("status"));
        return Result.ok();
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.resetPassword(id, body.get("password"));
        return Result.ok();
    }
}
