package com.takeout.module.user.service.impl;

import com.takeout.module.user.service.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseServiceImpl implements SseService {

    private final ConcurrentHashMap<Long, SseEmitter> connections = new ConcurrentHashMap<>();

    @Override
    public SseEmitter createConnection(Long userId) {
        SseEmitter emitter = new SseEmitter(0L);
        connections.put(userId, emitter);
        emitter.onCompletion(() -> connections.remove(userId));
        emitter.onTimeout(() -> connections.remove(userId));
        emitter.onError(e -> connections.remove(userId));
        log.info("SSE 连接建立: userId={}", userId);
        return emitter;
    }

    @Override
    public void sendEvent(Long userId, String eventName, Object data) {
        SseEmitter emitter = connections.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                connections.remove(userId);
                log.warn("SSE 推送失败: userId={}, event={}", userId, eventName);
            }
        }
    }
}
