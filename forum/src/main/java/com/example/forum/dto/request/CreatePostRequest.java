package com.example.forum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class CreatePostRequest {

//    @NotEmpty(message = "At least one category must be selected")
//    private Set<Long> categoryIds = new HashSet<>();

    @NotEmpty(message = "At least one tag must be selected")
    private Set<Long> tagIds = new HashSet<>();

    @NotBlank(message = "Post title cannot be blank")
    @Size(max = 255, message = "Post title cannot exceed 255 characters")
    private String postTitle;

    @NotBlank(message = "Post content cannot be blank")
    private String postContent;

    private String thumbnailUrl;

//    private List<MediaRequest> mediaRequestList;
}
