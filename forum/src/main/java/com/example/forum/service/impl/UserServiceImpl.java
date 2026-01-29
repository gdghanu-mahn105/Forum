package com.example.forum.service.impl;

import com.example.forum.common.constant.MessageConstants;
import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.dto.request.ChangePasswordRequest;
import com.example.forum.dto.request.UserUpdateRequest;
import com.example.forum.dto.response.PagedResponse;
import com.example.forum.dto.response.UserResponseDto;
import com.example.forum.entity.FollowId;
import com.example.forum.entity.UserEntity;
import com.example.forum.core.exception.ResourceNotFoundException;
import com.example.forum.repository.FollowRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FollowRepository followRepository;
    private final SecurityUtils securityUtils;

    public UserResponseDto getCurrentUser(UserEntity userEntity) {
        return mapToUserResponseDto(userEntity);
    }

    private UserResponseDto mapToUserResponseDto(UserEntity user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .userName(user.displayUsername())
                .avatarUrl(user.getAvatarUrl())
                .roles(user.getRoles())
                .isVerified(user.getIsVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public UserResponseDto getUserInfor(Long targetUserid) {
        UserEntity user = userRepository.findById(targetUserid)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));
        if(user.getIsDeleted()) {
            throw new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND);
        }
        int followerCount = followRepository.countFollowers(targetUserid);
        int followingCount = followRepository.countFollowings(targetUserid);
        boolean isFollowing = false;
        UserEntity currentUser = securityUtils.getCurrentUserOrNull();

        if(currentUser!= null && currentUser.getUserId() !=targetUserid ) {
            FollowId checkId = new FollowId(currentUser.getUserId(),targetUserid);

            isFollowing = followRepository.existsById(checkId);
        }
        UserResponseDto responseDto = mapToUserResponseDto(user);
        responseDto.setFollowerCount((long)followerCount);
        responseDto.setFollowingCount((long)followingCount);
        responseDto.setIsFollowing(isFollowing);

        return responseDto;
    }




    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public PagedResponse<UserResponseDto> getUsers(int page, int size, String sortBy, String sortDirect, String keyword) {

        Sort sort = sortDirect.equalsIgnoreCase("asc")
                ? Sort.by(ASC, sortBy)
                : Sort.by(DESC, sortBy);

        Pageable pageable= PageRequest.of(page,size,sort);


        if (keyword == null) keyword = "";

        Page<UserEntity> usersPage = userRepository.findByIsDeletedFalseAndUserNameContainingIgnoreCase(keyword, pageable);
        List<UserResponseDto> UserPageContent= usersPage.getContent().stream().map(this::mapToUserResponseDto).toList();
        return new PagedResponse<>(
                UserPageContent,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isLast()
        );
    }

    @Override
    public UserResponseDto updateUser(Long id, UserUpdateRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        if(request.getAvatarUrl()!= null && !request.getAvatarUrl().isBlank()){
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getUsername()!=null && !request.getUsername().isBlank()) {
            user.setUserName(request.getUsername());
        }

        userRepository.save(user);

        return mapToUserResponseDto(user);
    }

    @Override
    public void softDeleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException(MessageConstants.USER_NOT_FOUND));
        user.setIsDeleted(true);
        userRepository.save(user);
    }

    @Override
    public void hardDeleteUser(Long id) {
        UserEntity user= userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    @Override
    public void changePassword(Long id, ChangePasswordRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));
        if(!passwordEncoder.matches(request.getOldPassword(), user.getUserPassword())) {
            throw new IllegalArgumentException(MessageConstants.OLD_PASSWORD_INCORRECT);
        }
        user.setUserPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public UserResponseDto updateProfilePicture() {
        return null;
    }

    @Override
    public UserResponseDto updateUserInfo() {
        return null;
    }
}
