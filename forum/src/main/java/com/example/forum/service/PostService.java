package com.example.forum.service;

import com.example.forum.dto.request.CreatePostRequest;
import com.example.forum.dto.response.PostResponseDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PostService {
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    PostResponseDto createPost(CreatePostRequest request, Long userId);
}
