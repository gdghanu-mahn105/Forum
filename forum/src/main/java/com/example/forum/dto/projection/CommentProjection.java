package com.example.forum.dto.projection;

import java.time.LocalDateTime;

public interface CommentProjection {
    Long getCommentId();
    String getCommentContent();
    String getCommentPath();
    Boolean getIsDeleted();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();

    Long getUserId();
    String getUsername();
    String getEmail();
    String getAvatarUrl();

    Long getPostId();
    String getPostTitle();

    Long getReplyCount();
}

