package com.example.forum.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long commentId;
    private String commentContent;
    private String commentPath;
//    private Integer likes;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserSummaryDto userInfor;
    private PostInfo post;

    @Data
    @AllArgsConstructor
    public static class PostInfo {
        private Long postId;
        private String postTitle;
    }

    private Long replyCount;
}
