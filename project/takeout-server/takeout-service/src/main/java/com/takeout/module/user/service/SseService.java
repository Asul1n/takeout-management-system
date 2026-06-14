package com.takeout.module.user.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {

    /** 创建 SSE 连接 */
    SseEmitter createConnection(Long userId);

    /** 向指定用户推送事件 */
    void sendEvent(Long userId, String eventName, Object data);
}
