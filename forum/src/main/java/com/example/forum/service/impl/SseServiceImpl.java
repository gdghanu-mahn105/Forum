package com.example.forum.service.impl;

import com.example.forum.dto.response.NotificationDto;
import com.example.forum.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

    private final SseEmitter sseEmitter;

    @Value("${app.sse.timeout}")
    private Long emitterTimeout;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();


    @Override
    public SseEmitter subscribe(Long userId) {

        SseEmitter emitter = new SseEmitter(emitterTimeout);
        emitters.put(userId, emitter);

        emitter.onCompletion(()-> emitters.remove(userId));
        emitter.onTimeout(()-> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        return emitter;
    }

    @Async
    @Override
    public void sendRealTimeEvent(Long recipientId, NotificationDto notificationData) {
        SseEmitter sseEmitter = emitters.get(recipientId);
        if (sseEmitter == null) {
            return;
        }

        try{
            sseEmitter.send(
                    SseEmitter.event()
                            .name("notification_event")
                            .data(notificationData)
            );
        } catch (IOException e){
            emitters.remove(recipientId);
        }
    }
}
