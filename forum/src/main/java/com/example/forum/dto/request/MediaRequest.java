package com.example.forum.dto.request;

import com.example.forum.entity.Enum.MediaType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MediaRequest {
    @NotBlank(message = "Media URL cannot be blank")
    private String url;

    @NotBlank(message = "Media type cannot be blank")
    private MediaType type;

    private Long size;
}
