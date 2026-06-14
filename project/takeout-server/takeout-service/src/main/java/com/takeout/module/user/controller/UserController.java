package com.takeout.module.user.controller;

import com.takeout.common.result.Result;
import com.takeout.framework.security.UserDetailsImpl;
import com.takeout.module.user.dto.UpdatePasswordDTO;
import com.takeout.module.user.service.UserService;
import com.takeout.module.user.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户接口")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserInfoVO> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return Result.ok(userService.getCurrentUser(userDetails.getUserId()));
    }

    @Operation(summary = "修改个人信息（姓名/手机号）")
    @PutMapping("/me")
    public Result<UserInfoVO> updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @RequestBody Map<String, String> body) {
        return Result.ok(userService.updateProfile(userDetails.getUserId(), body));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @Valid @RequestBody UpdatePasswordDTO dto) {
        userService.updatePassword(userDetails.getUserId(), dto);
        return Result.ok();
    }
}
