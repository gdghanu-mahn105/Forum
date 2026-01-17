package com.example.forum.dto.projection;

import java.time.LocalDateTime;

public interface NotificationProjection {
    Long getNotificationId();
    Boolean getIsRead();
    Long getEventId();
    String getEventName();
    String getEventType();
    String getTargetUrl();
    LocalDateTime getDateNotice();
    Long getCreatedById();
    String getCreatedByName();
    String getCreatedByAvatar();
}
