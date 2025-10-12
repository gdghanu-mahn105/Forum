package com.example.forum.entity;

import com.example.forum.entity.Enum.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_event")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @Column(nullable = false, length = 100)
    private String eventName;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @Column(nullable = false)
    private LocalDateTime dateNotice;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String description;

    private Long referenceId;       //postId, commentId,...
    private String referenceType;

}
