package com.example.forum.service;

import com.example.forum.dto.projection.VoteProjection;
import com.example.forum.dto.response.PostVoteResponse;
import com.example.forum.entity.VoteType;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

public interface VoteService {
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    PostVoteResponse votePost(Long postId, VoteType voteType);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    List<VoteProjection> findVoteOfPost(Long postId, VoteType voteType);
}
