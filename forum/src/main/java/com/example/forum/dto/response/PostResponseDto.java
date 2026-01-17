package com.example.forum.dto.response;

import com.example.forum.entity.MediaEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class PostResponseDto {
    private Long postId;
    private String postTitle;
    private String postContent;
    private String thumbnailUrl;
    private List<MediaEntity> mediaEntityList;
    private Long upvotes;
    private Long downvotes;
    private Long countedViews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

//    private UserSummaryDto creator;
    private Long creatorId;
    private String creatorName;
    private String creatorAvatarUrl;

    private Set<CategoryDto> categories;
    private Set<TagDto> tags;

    private Long commentCount; // (Số bình luận)
    private Integer timeRead;   // (Thời gian đọc, giả sử là Integer)
    private String isVoted;     // ('UPVOTE', 'DOWNVOTE', hoặc null)
    private Boolean isSaved;    // (true/false)
}
