package com.example.forum.controller;

import com.example.forum.dto.request.ChangePasswordRequest;
import com.example.forum.dto.request.UserUpdateRequest;
import com.example.forum.dto.response.UserResponseDto;
import com.example.forum.entity.UserEntity;
import com.example.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/forum/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getCurrentUser(userEntity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserInfor(id));
    }

    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirect,
            @RequestParam(defaultValue = "") String keyword
    ){
        return ResponseEntity.ok(userService.getUsers(page, size, sortBy,sortDirect, keyword));
    }


    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword (
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request
            ) {
        return ResponseEntity.ok(userService.changePassword(id,request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> hardDeleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.hardDeleteUser(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.softDeleteUser(id));
    }
}
