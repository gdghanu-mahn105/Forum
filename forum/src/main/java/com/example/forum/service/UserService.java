package com.example.forum.service;

import com.example.forum.dto.request.ChangePasswordRequest;
import com.example.forum.dto.request.UserUpdateRequest;
import com.example.forum.dto.response.ApiResponse;
import com.example.forum.dto.response.PagedResponse;
import com.example.forum.dto.response.UserResponseDto;
import com.example.forum.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface UserService {

    @PreAuthorize("isAuthenticated()")
    UserResponseDto getCurrentUser(UserEntity userEntity);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    UserResponseDto getUserInfor(Long id);


    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    void softDeleteUser(Long id);

    @PreAuthorize("hasRole('ADMIN')")
    List<UserResponseDto> getAllUsers();

    @PreAuthorize("hasRole('ADMIN')")
    PagedResponse<UserResponseDto> getUsers(
            int page,
            int size,
            String sortBy,
            String sortDirect,
            String keyword
    );

    @PreAuthorize("hasRole('USER') and #id==authentication.principal.userId")
    UserResponseDto updateUser(Long id, UserUpdateRequest request);

    @PreAuthorize("hasRole('ADMIN')")
    void hardDeleteUser(Long id);

    @PreAuthorize("hasRole('USER') and #id==authentication.principal.userId")
    void changePassword(Long id, ChangePasswordRequest request);

    UserResponseDto updateProfilePicture();
    UserResponseDto updateUserInfo();
}
