package com.example.forum.dto.projection;

public interface VoteProjection {
    Long getUserId();
    String getUsername();
    String getAvatarUrl();
    int getValue();
}
