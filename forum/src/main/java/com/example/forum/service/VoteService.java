package com.example.forum.service;

import com.example.forum.dto.response.PostVoteResponse;

public interface VoteService {
    PostVoteResponse votePost(Long postId, int value);
}
