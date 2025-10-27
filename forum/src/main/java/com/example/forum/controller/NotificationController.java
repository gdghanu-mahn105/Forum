package com.example.forum.controller;

import com.example.forum.dto.response.ApiResponse;
import com.example.forum.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forum/user")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me/notification")
    ResponseEntity<?> getNotificationWithReadStatus(
            @RequestParam(defaultValue = "0",required = false) int page,
            @RequestParam(defaultValue = "10",required = false) int size,
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "false", required = false) Boolean isRead
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "List of notification",
                        notificationService.getNotificationsWithReadStatus(page,size,keyword,isRead)
                )
        );
    }

    @GetMapping("/me/notification/count")
    ResponseEntity<?> getNumberOfNotifications(){
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Number of unread notifications",
                        notificationService.countUnreadNotifications()
                )
        );
    }

    @PatchMapping("/me/notification/markAllRead")
    ResponseEntity<?> markAllRead(){
        notificationService.markAllAsRead();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Marked All notification as read",
                null
        ));
    }
    @PatchMapping("/me/notification/{id}")
    ResponseEntity<?> markAsRead(
            @PathVariable Long id){
        notificationService.markAsRead(id);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Marked as read",
                null
        ));
    }

    @PatchMapping("/me/notification/{id}/archive")
    public ResponseEntity<ApiResponse<Void>> archiveNotification(@PathVariable Long id) {
        notificationService.archiveNotification(id);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Notification archived",
                null
        ));
    }

    @DeleteMapping("/me/notification/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Notification deleted",
                null
        ));
    }


}
