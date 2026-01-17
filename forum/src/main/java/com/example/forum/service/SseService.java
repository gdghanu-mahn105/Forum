package com.example.forum.service;

import com.example.forum.dto.response.NotificationDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    SseEmitter subscribe(Long userId);
    void sendRealTimeEvent(Long recipientId, NotificationDto notificationData);

}
