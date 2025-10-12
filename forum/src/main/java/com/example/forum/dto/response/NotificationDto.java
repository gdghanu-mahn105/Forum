package com.example.forum.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long notificationId;
    private Boolean isRead;

    private Long eventId;
    private String eventName;
    private String eventType;
    private Long referenceId;
    private String referenceType;
    private LocalDateTime dateNotice;

    private Long createdById;
    private String createdByName;
    private String createdByAvatar;
}