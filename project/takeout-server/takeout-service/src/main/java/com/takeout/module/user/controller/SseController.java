package com.takeout.module.user.controller;

import com.takeout.framework.security.UserDetailsImpl;
import com.takeout.module.user.service.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "SSE 实时推送")
@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

    @Operation(summary = "SSE 连接端点")
    @GetMapping("/events")
    public SseEmitter subscribeEvents(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return sseService.createConnection(userDetails.getUserId());
    }
}
