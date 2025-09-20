package com.example.forum.service;

import com.example.forum.dto.projection.VoteProjection;
import com.example.forum.dto.response.PostVoteResponse;
import com.example.forum.entity.PostEntity;
import com.example.forum.entity.UserEntity;
import com.example.forum.entity.Vote;
import com.example.forum.entity.Enum.VoteType;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.VoteRepository;
import com.example.forum.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
public class VoteServiceIpml implements VoteService{

    private final PostRepository postRepository;
    private final SecurityService securityService;
    private final VoteRepository voteRepository;

    @Override
    public PostVoteResponse votePost(Long postId, VoteType newVote) {

        PostEntity post = postRepository.findByPostId(postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post not found!"));

        UserEntity currentUser = securityService.getCurrentUser();
        Long currentUserId= currentUser.getUserId();

        var existingVote = voteRepository.findByUserEntityUserIdAndPostEntityPostId(currentUserId, postId);

        VoteType finalVote;

        if(existingVote.isEmpty()) {
            finalVote = createVote(post, currentUser, newVote);
        } else {
            Vote vote = existingVote.get();
            if (vote.getVoteType() == newVote) {
                finalVote = cancelVote(post, vote, newVote);
            } else {
                finalVote = changeVote(post, vote, newVote);
            }
        }

        postRepository.save(post);

        return new PostVoteResponse(
                post.getPostId(),
                post.getUpvotes(),
                post.getDownvotes(),
                post.getUpvotes() - post.getDownvotes(),
                finalVote
        );
    }

    @Override
    public List<VoteProjection> findVoteOfPost(Long postId, VoteType voteType) {
        PostEntity post = postRepository.findByPostId(postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post not found!"));
        return voteRepository.findVotesOfPost(postId, voteType.name());
    }

    private VoteType createVote (PostEntity post, UserEntity user, VoteType newVote) {
        Vote vote = Vote.builder()
                .postEntity(post)
                .voteType(newVote)
                .userEntity(user)
                .build();

        if (newVote == VoteType.UPVOTE) post.setUpvotes(post.getUpvotes() + 1);
        else if (newVote == VoteType.DOWNVOTE) post.setDownvotes(post.getDownvotes() + 1);

        voteRepository.save(vote);
        return newVote;
    }

    private VoteType cancelVote(PostEntity post, Vote existingVote, VoteType currentVote) {
        voteRepository.delete(existingVote);
        if (currentVote == VoteType.UPVOTE) post.setUpvotes(Math.max(0, post.getUpvotes() - 1));
        else if (currentVote == VoteType.DOWNVOTE) post.setDownvotes(Math.max(0, post.getDownvotes() - 1));
        return VoteType.NONE;
    }

    private VoteType changeVote (PostEntity post, Vote existingVote, VoteType newVote) {
        if (newVote == VoteType.UPVOTE) {
            post.setDownvotes(Math.max(0, post.getDownvotes() - 1));
            post.setUpvotes(post.getUpvotes() + 1);
        } else if (newVote == VoteType.DOWNVOTE) {
            post.setUpvotes(Math.max(0, post.getUpvotes() - 1));
            post.setDownvotes(post.getDownvotes() + 1);
        }

        existingVote.setVoteType(newVote);
        voteRepository.save(existingVote);
        return newVote;
    }
}
