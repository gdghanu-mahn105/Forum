package com.example.forum.service.impl;

import com.example.forum.dto.projection.NotificationProjection;
import com.example.forum.dto.response.NotificationDto;
import com.example.forum.dto.response.PagedResponse;
import com.example.forum.entity.Enum.EventType;
import com.example.forum.entity.Notification;
import com.example.forum.entity.NotificationEvent;
import com.example.forum.entity.UserEntity;
import com.example.forum.exception.NotLoggedInException;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.repository.EventNotificationRepository;
import com.example.forum.repository.FollowRepository;
import com.example.forum.repository.NotificationRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.utils.SecurityUtils;
import com.example.forum.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final EventNotificationRepository eventRepository;
    private final NotificationRepository notificationRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityService;

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



        NotificationEvent notificationEvent= NotificationEvent.builder()
                .eventName(eventName)
                .createdBy(creator)
                .eventType(eventType)
                .dateNotice(LocalDateTime.now())
                .description(description)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();


        return eventRepository.save(notificationEvent);
    }

    @Override
    public void notifyFollowers(NotificationEvent event) {
        if(event.getEventType()== EventType.NEW_POST){
            Long creatorId = event.getCreatedBy().getUserId();
            List<Long> followerIdList = followRepository.findFollowerUserIdByFollowingUserId(creatorId);
            if(followerIdList.isEmpty()) return;
//            List<Notification> newNotificationList= new ArrayList<>();
//            for( Long id : followerIdList) {
//                Notification newNotice = Notification.builder()
//                        .notificationEvent(event)
//                        .userEntity(event.getCreatedBy())
//                        .isRead(false)
//                        .isArchived(false)
//                        .build();
//                newNotificationList.add(newNotice);
//            }

            List<UserEntity> followers = userRepository.findAllById(followerIdList);
            List<Notification> newNotificationList= followers.stream()
                            .map(follower -> Notification.builder()
                                    .notificationEvent(event)
                                    .userEntity(follower)
                                    .isRead(false)
                                    .isArchived(false)
                                    .build()
                            ).toList();
            notificationRepository.saveAll(newNotificationList);
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

        notificationRepository.save(notification);
    }

    @Override
    public PagedResponse<NotificationDto> getNotificationsWithReadStatus(int page, int size, String keyword, Boolean isRead) {
        if (keyword == null) {
            keyword = "";
        }
        if(isRead==null){
            isRead=false;
        }
        Pageable pageable = PageRequest.of(page, size);

        Long currentUserId= securityService.getCurrentUser().getUserId();

        Page<NotificationProjection> notiProjectionListPage= notificationRepository.findUnreadNotificationsByUserIdAndReadStatus(currentUserId,isRead, keyword, pageable);
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
            throw new NotLoggedInException("User not logged in");
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
            throw new NotLoggedInException("User not logged in");
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
                .orElseThrow(()-> new ResourceNotFoundException("Notification not found!"));
    }

    private List<NotificationDto> mapToNotificationDto(Page<NotificationProjection> listPage){
        return listPage.stream()
                .map(p -> NotificationDto.builder()
                        .notificationId(p.getNotificationId())
                        .isRead(p.getIsRead())
                        .eventId(p.getEventId())
                        .eventName(p.getEventName())
                        .eventType(p.getEventType())
                        .referenceId(p.getReferenceId())
                        .referenceType(p.getReferenceType())
                        .dateNotice(p.getDateNotice())
                        .createdById(p.getCreatedById())
                        .createdByName(p.getCreatedByName())
                        .createdByAvatar(p.getCreatedByAvatar())
                        .build())
                .toList();
    }
}
