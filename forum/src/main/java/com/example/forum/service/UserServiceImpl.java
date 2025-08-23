package com.example.forum.service;

import com.example.forum.dto.request.ChangePasswordRequest;
import com.example.forum.dto.request.UserUpdateRequest;
import com.example.forum.dto.response.ApiResponse;
import com.example.forum.dto.response.PagedResponse;
import com.example.forum.dto.response.UserResponseDto;
import com.example.forum.entity.UserEntity;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.repository.UserRepository;
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
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto getCurrentUser(UserEntity userEntity) {
        return mapToUserResponseDto(userEntity);
    }

    private UserResponseDto mapToUserResponseDto(UserEntity user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .userName(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .roles(user.getRoles())
                .isVerified(user.getIsVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public UserResponseDto getUserInfor(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if(user.getIsDeleted()) {
            throw new ResourceNotFoundException("User not found!");
        }
        return mapToUserResponseDto(user);
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

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
    public ApiResponse<?> softDeleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("User not found!"));
        user.setIsDeleted(true);
        userRepository.save(user);
        return ApiResponse.builder()
                .success(true)
                .message("User is deleted")
                .build();
    }

    @Override
    public ApiResponse<?> hardDeleteUser(Long id) {
        UserEntity user= userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found!"));
        userRepository.delete(user);
        return ApiResponse.builder()
                .success(true)
                .message("User is permanently deleted")
                .build();
    }

    @Override
    public ApiResponse<?> changePassword(Long id, ChangePasswordRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found!"));
        if(!passwordEncoder.matches(request.getOldPassword(), user.getUserPassword())) {
            throw new IllegalArgumentException("Old password is incorrect!");
        }
        user.setUserPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ApiResponse.builder()
                .success(true)
                .message("Password is successfully changed!")
                .build();
    }
}
