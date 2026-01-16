package com.example.forum.service.impl;

import com.example.forum.dto.response.NotificationDto;
import com.example.forum.service.SseService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseServiceImpl implements SseService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    public final Long EMITTER_TIMEOUT = 1800000L;

    @Override
    public SseEmitter subscribe(Long userId) {

        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
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
