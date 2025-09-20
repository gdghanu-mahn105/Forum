package com.example.forum.dto.response;

import com.example.forum.entity.VoteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@AllArgsConstructor
public class PostVoteResponse {
    private Long postId;
    private Long upvotes;
    private Long downvotes;
    private Long score;
    private VoteType userVote;
}
