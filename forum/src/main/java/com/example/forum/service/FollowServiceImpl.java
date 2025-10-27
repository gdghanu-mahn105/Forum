package com.example.forum.service;

import com.example.forum.dto.projection.UserSummaryProjection;
import com.example.forum.dto.response.PagedResponse;
import com.example.forum.dto.response.UserSummaryDto;
import com.example.forum.entity.Enum.EventType;
import com.example.forum.entity.Follow;
import com.example.forum.entity.FollowId;
import com.example.forum.entity.NotificationEvent;
import com.example.forum.entity.UserEntity;
import com.example.forum.exception.BadRequestException;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.repository.FollowRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService{

    private final SecurityService securityService;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public void followUser(Long followingId) {

        UserEntity following = userRepository.findById(followingId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found!"));

        UserEntity follower = securityService.getCurrentUser(); // user
        Long currentFollowerId= follower.getUserId();

        if(currentFollowerId==followingId){
            throw new BadRequestException("You can not follow yourself!");
        }

        FollowId followId = new FollowId(currentFollowerId, followingId);
        if(!followRepository.existsById(followId)){
            Follow newFollow = new Follow(followId,follower,following, LocalDateTime.now());
            followRepository.save(newFollow);
        } else {
            throw new BadRequestException("You already followed this user");
        }

        NotificationEvent newNotificationEvent = notificationService.createEvent(
                EventType.NEW_FOLLOWER,
                follower,
                null,
                following.getUserId(),
                "USER");

        notificationService.notifyFollowers(newNotificationEvent);

    }

    @Override
    public PagedResponse<UserSummaryDto> getFollowers(int page, int size,String keyword) {
        UserEntity follower = securityService.getCurrentUser(); // user
        Long currentFollowerId= follower.getUserId();

        if (keyword == null) {
            keyword = "";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<UserSummaryProjection> listOfFollowerPage = followRepository.findFollowerByFollowingIdAndUsername(currentFollowerId,keyword,pageable);
        List<UserSummaryDto> listOfFollowers= listOfFollowerPage.getContent().stream()
                .map(f -> new UserSummaryDto(
                        f.getUserId(),
                        f.getUserName(),
                        f.getEmail(),
                        f.getAvatarUrl()
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                listOfFollowers,
                listOfFollowerPage.getNumber(),
                listOfFollowerPage.getSize(),
                listOfFollowerPage.getTotalElements(),
                listOfFollowerPage.getTotalPages(),
                listOfFollowerPage.isLast()
        );
    }

    @Override
    public PagedResponse<UserSummaryDto> getFollowings(int page, int size, String keyword) {
        UserEntity following = securityService.getCurrentUser(); //this user
        Long currentFollowingId= following.getUserId();

        if (keyword == null) {
            keyword = "";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<UserSummaryProjection> listOfFollowingWithPage = followRepository.findFollowingUserIdByFollowerUserId(currentFollowingId,keyword,pageable);
        List<UserSummaryDto> listOfFollowings= listOfFollowingWithPage.getContent().stream()
                .map(f -> new UserSummaryDto(
                        f.getUserId(),
                        f.getUserName(),
                        f.getEmail(),
                        f.getAvatarUrl()
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                listOfFollowings,
                listOfFollowingWithPage.getNumber(),
                listOfFollowingWithPage.getSize(),
                listOfFollowingWithPage.getTotalElements(),
                listOfFollowingWithPage.getTotalPages(),
                listOfFollowingWithPage.isLast()
        );
    }

    @Override
    public int getNumberOfFollower(Long followingId) {
        getUserOrThrow(followingId);
        return followRepository.countFollowers(followingId);
    }

    @Override
    public int getNumberOfFollowing(Long followerId) {
        getUserOrThrow(followerId);
        return followRepository.countFollowings(followerId);
    }

    @Override
    public void unfollow(Long id) {

        getUserOrThrow(id);

        UserEntity follower = securityService.getCurrentUser(); // user
        Long currentFollowerId= follower.getUserId();

        FollowId followId = new FollowId(currentFollowerId, id);

        Optional<Follow> existingFollowId = followRepository.findById(followId);

        if(existingFollowId.isPresent()){
            Follow existFollow= (Follow) existingFollowId.get();
            followRepository.delete(existFollow);
        } else  {
            throw new BadRequestException("You haven't followed this user");
        }
    }

    @Override
    public void removeFollower(Long id) {

        getUserOrThrow(id);

        UserEntity currentUser = securityService.getCurrentUser();
        Long currentUserId = currentUser.getUserId();

        FollowId followId = new FollowId(id, currentUserId);

        Optional<Follow> existingFollow = followRepository.findById(followId);
        if (existingFollow.isPresent()) {
            followRepository.delete(existingFollow.get());
        } else {
            throw new BadRequestException("This user is not following you");
        }
    }

    private UserEntity getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
