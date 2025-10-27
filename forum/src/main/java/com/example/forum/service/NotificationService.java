package com.example.forum.service;

import com.example.forum.dto.response.NotificationDto;
import com.example.forum.dto.response.PagedResponse;
import com.example.forum.entity.Enum.EventType;
import com.example.forum.entity.NotificationEvent;
import com.example.forum.entity.UserEntity;

public interface NotificationService {
    NotificationEvent createEvent(EventType eventType, UserEntity creator, String description, Long refereneId, String referenceType);

    void notifyFollowers(NotificationEvent event);

    void notifySpecificUser(UserEntity receiver,NotificationEvent event);

    PagedResponse<NotificationDto> getNotificationsWithReadStatus(int page, int size, String keyword,Boolean isRead);

    Long countUnreadNotifications();

    void markAsRead(Long notificationId);

    void markAllAsRead();

    void archiveNotification(Long notificationId);

    void deleteNotification(Long notificationId);

}
