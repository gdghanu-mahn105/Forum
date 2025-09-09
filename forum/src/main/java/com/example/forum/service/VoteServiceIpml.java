package com.example.forum.service;

import com.example.forum.dto.response.PostVoteResponse;
import com.example.forum.entity.PostEntity;
import com.example.forum.entity.UserEntity;
import com.example.forum.entity.Vote;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.VoteRepository;
import com.example.forum.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoteServiceIpml implements VoteService{

    private final PostRepository postRepository;
    private final SecurityService securityService;
    private final VoteRepository voteRepository;

    @Override
    public PostVoteResponse votePost(Long postId, int value) {
        PostEntity post = postRepository.findByPostId(postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post not found!"));
        if (value != -1 && value != 1) {
            throw new IllegalArgumentException("Vote must be -1 or +1");
        }

        UserEntity currentuUser = securityService.getCurrentUser();
        Long currentUserId= currentuUser.getUserId();

        var existingVote = voteRepository.findByUserEntityUserIdAndPostEntityPostId(currentUserId, postId);

        if(existingVote.isEmpty()) {
            Vote newVote = Vote.builder()
                    .userEntity(currentuUser)
                    .postEntity(post)
                    .value(value)
                    .build();
            voteRepository.save(newVote);
            if (value == 1) post.setUpvotes(post.getUpvotes() + 1);
            else if (value == -1) post.setDownvotes(post.getDownvotes() + 1);
        } else {
            Vote vote = existingVote.get();
            // cancel vote
            if(vote.getValue()==value) {
                if (value == 1) post.setUpvotes(post.getUpvotes() - 1);
                else if (value == -1) post.setDownvotes(post.getDownvotes() - 1);
                value=0;
                voteRepository.delete(vote);
            } else { // if voteNow = -1 1
                //change vote
                if (vote.getValue()==1) post.setUpvotes(post.getUpvotes()-1);
                else if(vote.getValue()==-1) post.setDownvotes(post.getDownvotes()-1);

                if(value ==1) post.setUpvotes(post.getUpvotes()+1);
                else if (value == -1) post.setDownvotes(post.getDownvotes()+1);
                vote.setValue(value);
                voteRepository.save(vote);
            }

        }
        postRepository.save(post);

        return new PostVoteResponse(
                post.getPostId(),
                post.getUpvotes(),
                post.getDownvotes(),
                post.getUpvotes() - post.getDownvotes(),
                value
        );
    }
}
