package com.takeout.module.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.common.result.Result;
import com.takeout.framework.security.UserDetailsImpl;
import com.takeout.module.user.entity.Notification;
import com.takeout.module.user.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "通知接口")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "获取通知列表")
    @GetMapping
    public Result<Page<Notification>> list(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(notificationService.list(userDetails.getUserId(), page, size));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                  @PathVariable Long id) {
        notificationService.markRead(userDetails.getUserId(), id);
        return Result.ok();
    }

    @Operation(summary = "全部标记已读")
    @PutMapping("/read-all")
    public Result<Void> markAllRead(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        notificationService.markAllRead(userDetails.getUserId());
        return Result.ok();
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return Result.ok(notificationService.unreadCount(userDetails.getUserId()));
    }
}
