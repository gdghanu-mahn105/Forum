package com.example.forum.dto.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime; // QUAN TRỌNG: Dùng OffsetDateTime cho múi giờ (+00:00)
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("content")
    private String content;

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("owner_id")
    private Long ownerId;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt; // Dùng OffsetDateTime

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt; // Dùng OffsetDateTime

    @JsonProperty("upvote")
    private Long upvote;

    @JsonProperty("downvote")
    private Long downvote;

    @JsonProperty("child_comment_count")
    private Long childCommentCount; // Khớp với 'replyCount' của bạn

    @JsonProperty("is_archived")
    private Boolean isArchived; // Khớp với 'isDeleted' của bạn

    @JsonProperty("path")
    private String path;

    @JsonProperty("depth")
    private Integer depth;

    @JsonProperty("parent_path")
    private String parentPath;

    @JsonProperty("owner") // Đây là một object lồng nhau
    private CommentOwnerDto owner; // Chúng ta sẽ tạo file DTO này ở dưới

    @JsonProperty("user_vote_type")
    private String userVoteType;

    @JsonProperty("children") // Luôn trả về một mảng, dù rỗng
    private List<CommentDto> children; // (Dành cho comment trả lời lồng nhau)
}
