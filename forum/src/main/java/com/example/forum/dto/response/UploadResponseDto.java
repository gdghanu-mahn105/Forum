package com.example.forum.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadResponseDto {
    private String id;
    private String url;
    private String format;
    private String resourceType;
    private Long bytes;
}
