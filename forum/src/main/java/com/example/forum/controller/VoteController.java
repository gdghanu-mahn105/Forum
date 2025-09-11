package com.example.forum.controller;

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
            @RequestParam int value
    ){
        return ResponseEntity.ok(voteService.votePost(post_id,value));
    }

    @GetMapping("/{post_id}/vote")
    ResponseEntity<?> getVoteByVoteType (
            @PathVariable Long post_id,
            @RequestParam int voteValue
    ) {
        return ResponseEntity.ok(voteService.findVoteOfPost(post_id, voteValue));
    }
}
