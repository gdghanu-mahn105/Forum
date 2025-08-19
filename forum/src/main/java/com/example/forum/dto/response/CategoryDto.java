package com.example.forum.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDto {
    private Long categoryId;
    private String categoryName;
}