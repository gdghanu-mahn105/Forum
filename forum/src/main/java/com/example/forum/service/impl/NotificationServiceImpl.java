package com.example.forum.service.impl;

import com.example.forum.common.constant.MessageConstants;
import com.example.forum.dto.projection.NotificationProjection;
import com.example.forum.dto.response.NotificationDto;
import com.example.forum.dto.response.PagedResponse;
import com.example.forum.entity.Enum.EventType;
import com.example.forum.entity.Notification;
import com.example.forum.entity.NotificationEvent;
import com.example.forum.entity.UserEntity;
import com.example.forum.core.exception.NotLoggedInException;
import com.example.forum.core.exception.ResourceNotFoundException;
import com.example.forum.repository.EventNotificationRepository;
import com.example.forum.repository.FollowRepository;
import com.example.forum.repository.NotificationRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.service.NotificationService;
import com.example.forum.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final EventNotificationRepository eventRepository;
    private final NotificationRepository notificationRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    private final SecurityUtils securityService;
    private final SseService sseService;

    @Override
    public NotificationEvent createEvent(EventType eventType, UserEntity creator, String description, Long referenceId, String referenceType) {
        String creatorName= creator.displayUsername();

        String eventName = switch (eventType) {
            case NEW_POST -> "New post created by " + creatorName;
            case NEW_COMMENT -> "New comment from " + creatorName;
            case NEW_REPLY -> "New reply from " + creatorName;
            case NEW_VOTE -> "New vote by " + creatorName;
            case NEW_FOLLOWER -> creatorName +" have followed you";
        };

        String targetUrl = createTargetUrl(eventType, referenceId, creator.getUserId());

        NotificationEvent notificationEvent= NotificationEvent.builder()
                .eventName(eventName)
                .createdBy(creator)
                .eventType(eventType)
                .dateNotice(LocalDateTime.now())
                .targetUrl(targetUrl)
                .description(description)
                .build();

        NotificationEvent savedEvent= eventRepository.save(notificationEvent);

        dispatchNotification(savedEvent, referenceId);


        return savedEvent;
    }


    @Override
    public void notifyFollowers(NotificationEvent event) {
        if(event.getEventType()== EventType.NEW_POST){
            Long creatorId = event.getCreatedBy().getUserId();
            List<Long> followerIdList = followRepository.findFollowerUserIdByFollowingUserId(creatorId);
            if(followerIdList.isEmpty()) return;

            List<UserEntity> followers = userRepository.findAllById(followerIdList);
            List<Notification> newNotificationList= followers.stream()
                            .map(follower -> Notification.builder()
                                    .notificationEvent(event)
                                    .userEntity(follower)
                                    .isRead(false)
                                    .isArchived(false)
                                    .build()
                            ).toList();
            List<Notification> savedNoti = notificationRepository.saveAll(newNotificationList);

            for (Notification noti : savedNoti){
                NotificationDto notificationDto = mapSingleToDto(noti);
                sseService.sendRealTimeEvent(noti.getUserEntity().getUserId(), notificationDto);
            }
        }
    }

    @Override
    public void notifySpecificUser(UserEntity receiver, NotificationEvent event) {
        if (receiver == null || event == null) return;

        Notification notification = Notification.builder()
                .notificationEvent(event)
                .userEntity(receiver)
                .isRead(false)
                .isArchived(false)
                .build();

        Notification savedNoti = notificationRepository.save(notification);

        NotificationDto dto = mapSingleToDto(savedNoti);
        sseService.sendRealTimeEvent(receiver.getUserId(), dto);
    }

    @Override
    public PagedResponse<NotificationDto> getNotificationsWithReadStatus(int page, int size, String keyword, Boolean isRead) {
        if (keyword == null) {
            keyword = "";
        }

        Pageable pageable = PageRequest.of(page, size);

        Long currentUserId= securityService.getCurrentUser().getUserId();

        Page<NotificationProjection> notiProjectionListPage= notificationRepository.findNotificationsByUserIdAndReadStatus(currentUserId,isRead, keyword, pageable);
        List<NotificationDto> notificationDtoList = mapToNotificationDto(notiProjectionListPage);

        return new PagedResponse<>(
                notificationDtoList,
                notiProjectionListPage.getNumber(),
                notiProjectionListPage.getSize(),
                notiProjectionListPage.getTotalElements(),
                notiProjectionListPage.getTotalPages(),
                notiProjectionListPage.isLast()
        );
    }

    @Override
    public Long countUnreadNotifications() {

        UserEntity currentUser= securityService.getCurrentUser();
        if (currentUser == null) {
            throw new NotLoggedInException(MessageConstants.LOGIN_REQUIRED);
        }
        return notificationRepository.countByUserEntityUserIdAndIsReadFalse(currentUser.getUserId());
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification existingNotification = checkNotificationExist(notificationId);
        existingNotification.setIsRead(true);
        notificationRepository.save(existingNotification);
    }

    @Override
    public void markAllAsRead() {
        UserEntity currentUser= securityService.getCurrentUser();
        if (currentUser == null) {
            throw new NotLoggedInException(MessageConstants.LOGIN_REQUIRED);
        }
        List<Notification> readList =notificationRepository.findAllByUserEntityUserIdAndIsReadFalse(currentUser.getUserId())
                .stream().peek(n-> n.setIsRead(true)).toList();
        notificationRepository.saveAll(readList);
    }

    @Override
    public void archiveNotification(Long notificationId) {
        Notification existingNotification = checkNotificationExist(notificationId);
        existingNotification.setIsArchived(true);
        notificationRepository.save(existingNotification);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        Notification existingNotification = checkNotificationExist(notificationId);
        notificationRepository.delete(existingNotification);
    }

    private Notification checkNotificationExist(Long id) {
        return notificationRepository.findByIdAndIsArchivedFalse(id)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.NOTIFICATION_NOT_FOUND));
    }

    private List<NotificationDto> mapToNotificationDto(Page<NotificationProjection> listPage){
        return listPage.stream()
                .map(p -> NotificationDto.builder()
                        .notificationId(p.getNotificationId())
                        .isRead(p.getIsRead())
                        .eventId(p.getEventId())
                        .eventName(p.getEventName())
                        .eventType(p.getEventType())
                        .dateNotice(p.getDateNotice())
                        .createdById(p.getCreatedById())
                        .targetUrl(p.getTargetUrl())
                        .createdByName(p.getCreatedByName())
                        .createdByAvatar(p.getCreatedByAvatar())
                        .build())
                .toList();
    }
    private NotificationDto mapSingleToDto(Notification n) {
        NotificationEvent e = n.getNotificationEvent();
        UserEntity creator = e.getCreatedBy();

        return NotificationDto.builder()
                .notificationId(n.getId())
                .isRead(n.getIsRead())
                .eventId(e.getEventId())
                .eventName(e.getEventName())
                .eventType(e.getEventType().toString())
                .dateNotice(e.getDateNotice())
                .createdById(creator.getUserId())
                .targetUrl(e.getTargetUrl())
                .createdByName(creator.displayUsername())
                .createdByAvatar(creator.getAvatarUrl())
                .build();
    }

    private String createTargetUrl(EventType eventType, Long referenceId, Long creatorId){
        String targetUrl="";
        switch (eventType) {
            case NEW_POST:
            case NEW_VOTE:
            case NEW_COMMENT:
            case NEW_REPLY:
                targetUrl = "/posts/" + referenceId;
                break;

            case NEW_FOLLOWER:
                targetUrl = "/users/" + creatorId;
                break;

            default:
                targetUrl = "/home";
        }
        return targetUrl;
    }


    private void dispatchNotification(NotificationEvent event, Long referenceId){
        switch (event.getEventType()) {
            case NEW_POST:
                this.notifyFollowers(event);
                break;

            case NEW_FOLLOWER:
                userRepository.findById(referenceId).ifPresent(receiver ->
                        this.notifySpecificUser(receiver, event)
                );
                break;
            default:
                log.warn("Unhandled event type: {}", event.getEventType());
        }
    }

}
