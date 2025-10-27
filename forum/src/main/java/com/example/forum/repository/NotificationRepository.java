package com.example.forum.repository;

import com.example.forum.dto.projection.NotificationProjection;
import com.example.forum.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {

    @Query(value = """
            SELECT
                n.id AS notificationId,
                n.is_read AS isRead,

                e.event_id AS eventId,
                e.event_name AS eventName,
                e.event_type AS eventType,
                e.reference_id AS referenceId,
                e.reference_type AS referenceType,
                e.date_notice AS dateNotice,

                u.user_id AS createdById,
                u.user_name AS createdByName,
                u.avatar_url AS createdByAvatar
            FROM notification n
            JOIN notification_event e ON n.event_id = e.event_id
            JOIN users u ON e.created_by = u.user_id
            WHERE n.user_id = :userId
              AND n.is_read = :isRead
              AND e.event_name like concat ('%', :keyword,'%')
              AND n.is_archived = false
            ORDER BY e.date_notice DESC
        """,
            countQuery = """
            SELECT COUNT(*)
            FROM notification n
            JOIN notification_event e ON n.event_id = e.event_id
            WHERE n.user_id = :userId
              AND n.is_read = :isRead
              AND n.is_archived = false
            """,
            nativeQuery = true)
    Page<NotificationProjection> findUnreadNotificationsByUserIdAndReadStatus(
            @Param("userId") Long userId,
            @Param("isRead") boolean isRead,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByUserEntityUserIdAndIsReadFalse(Long userId);

    Optional<Notification> findByIdAndIsArchivedFalse(Long id);

    List<Notification> findAllByUserEntityUserIdAndIsReadFalse(Long userId);
}
