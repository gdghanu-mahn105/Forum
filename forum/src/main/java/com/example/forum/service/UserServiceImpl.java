package com.example.forum.service;

import com.example.forum.dto.request.ChangePasswordRequest;
import com.example.forum.dto.request.UserUpdateRequest;
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
    public Page<UserResponseDto> getUsers(int page, int size, String sortBy,String sortDirect, String keyword) {

        Sort sort = sortDirect.equalsIgnoreCase("asc")
                ? Sort.by(ASC, sortBy)
                : Sort.by(DESC, sortBy);

        Pageable pageable= PageRequest.of(page,size,sort);


        if (keyword == null) keyword = "";

        Page<UserEntity> users = userRepository.findByIsDeletedFalseAndUserNameContainingIgnoreCase(keyword, pageable);
        return users.map(this::mapToUserResponseDto);
    }

    @Override
    public UserResponseDto updateUser(Long id, UserUpdateRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        user.setUserName(request.getUsername());
        user.setAvatarUrl(request.getAvatarUrl());
        userRepository.save(user);

        return mapToUserResponseDto(user);
    }

    @Override
    public String softDeleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("User not found!"));
        user.setIsDeleted(true);
        userRepository.save(user);
        return "Delete user with id " + id + "SUCCESSFULLY";
    }

    @Override
    public String hardDeleteUser(Long id) {
        UserEntity user= userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found!"));
        userRepository.delete(user);
        return "Permanently deleted user!";
    }

    @Override
    public String changePassword(Long id, ChangePasswordRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found!"));
        if(!passwordEncoder.matches(request.getOldPassword(), user.getUserPassword())) {
            throw new IllegalArgumentException("Old password is incorrect!");
        }
        user.setUserPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Password is successfully changed!";
    }
}
