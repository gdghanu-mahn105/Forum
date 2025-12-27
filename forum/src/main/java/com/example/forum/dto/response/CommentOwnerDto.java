package com.example.forum.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentOwnerDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("photo")
    private String photo;

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("point")
    private Double point; // Số thập phân dùng Double

    @JsonProperty("bio")
    private String bio;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
