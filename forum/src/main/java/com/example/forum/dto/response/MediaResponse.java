package com.example.forum.dto.response;

import com.example.forum.entity.Enum.MediaType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaResponse {
    private Long id;
    private String url;
    private MediaType type;
    private Long size;
}
