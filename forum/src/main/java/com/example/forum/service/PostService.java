package com.example.forum.service;

import com.example.forum.dto.request.CreatePostRequest;
import com.example.forum.dto.request.UpdatePostRequest;
import com.example.forum.dto.response.PagedResponse;
import com.example.forum.dto.response.PostResponseDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PostService {
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    PostResponseDto createPost(CreatePostRequest request);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    PostResponseDto getPost(Long postId);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    PagedResponse<PostResponseDto> getPosts (
            int page,
            int size,
            String sortBy,
            String sortDirect,
            String keyword
    );

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    PostResponseDto updatePost(Long postId,UpdatePostRequest request);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    void softDeletePost(Long id);

    @PreAuthorize("hasAnyRole('ADMIN')")
    void hardDeletePost(Long id);

    @PreAuthorize("hasAnyRole('ADMIN')")
    void removeMediaFromPost(Long postId, Long mediaId);
}
