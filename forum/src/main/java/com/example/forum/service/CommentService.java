package com.example.forum.service;

import com.example.forum.dto.request.CreateCommentRequest;
import com.example.forum.dto.request.UpdateCommentRequest;
import com.example.forum.dto.response.ApiResponse;
import com.example.forum.dto.response.CommentDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface CommentService {
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    CommentDto createComment(Long postId, CreateCommentRequest request);

    List<CommentDto> getListOfCommentByPath(Long postId,String path);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    List<CommentDto> getListOfCommentAndCountReplyComment(Long postId);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    CommentDto updateComment(Long commentId, UpdateCommentRequest request);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    void softDeletedComment(Long commentId);

    @PreAuthorize("hasRole('ADMIN')")
    void hardDeletedComment(Long commentId);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    List<CommentDto> getListOfCommentAndCountReplyComment(Long postId, String parentPath, Long parentId);

}
