package com.example.forum.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class PostResponseDto {
    private Long postId;
    private String postTitle;
    private String postContent;
    private String thumbnailUrl;
    private Long upvotes;
    private Long downvotes;
    private Long countedViews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserSummaryDto creator;

    private Set<CategoryDto> categories;
    private Set<TagDto> tags;
}
