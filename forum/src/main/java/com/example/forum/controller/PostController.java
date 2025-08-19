package com.example.forum.controller;

import com.example.forum.dto.request.CreatePostRequest;
import com.example.forum.dto.response.PostResponseDto;
import com.example.forum.entity.UserEntity;
import com.example.forum.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forum/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<?> createPost (
            @RequestBody CreatePostRequest request,
            Authentication authentication
            ) {
        UserEntity user =(UserEntity) authentication.getPrincipal();
        Long userId = user.getUserId();
        PostResponseDto postResponse = postService.createPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(postResponse);

    }
}
