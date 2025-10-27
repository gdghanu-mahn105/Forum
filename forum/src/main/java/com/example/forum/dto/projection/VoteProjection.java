package com.example.forum.dto.projection;

import com.example.forum.entity.Enum.VoteType;

public interface VoteProjection {
    Long getUserId();
    String getUsername();
    String getAvatarUrl();
    VoteType getVoteType();
}
