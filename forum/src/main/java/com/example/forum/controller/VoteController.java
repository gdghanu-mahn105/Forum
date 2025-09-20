package com.example.forum.controller;

import com.example.forum.dto.response.ApiResponse;
import com.example.forum.entity.Enum.VoteType;
import com.example.forum.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forum/posts")
public class VoteController {
    private final VoteService voteService;

    @PostMapping("/{post_id}/vote")
    ResponseEntity<?> vote (
            @PathVariable Long post_id,
            @RequestParam VoteType voteType
    ){
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Vote success",
                voteService.votePost(post_id, voteType)
        ));
    }

    @GetMapping("/{post_id}/vote")
    ResponseEntity<?> getVoteByVoteType (
            @PathVariable Long post_id,
            @RequestParam VoteType voteType
            ) {

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "list of vote by vote_type",
                voteService.findVoteOfPost(post_id, voteType)
        ));
    }
}
