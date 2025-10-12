package com.example.forum.repository;

import com.example.forum.entity.NotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventNotificationRepository extends JpaRepository<NotificationEvent, Long> {
}
