package com.example.forum.controller;


import com.example.forum.dto.request.CreateCommentRequest;
import com.example.forum.dto.request.UpdateCommentRequest;
import com.example.forum.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forum/post/comment")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/create")
    ResponseEntity<?> createComment(
            @RequestParam Long postId,
            @RequestBody CreateCommentRequest request
            ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commentService.createComment(postId,request));
    }
    @GetMapping("/getCommentCount")
    ResponseEntity<?> getCommentWithReplyCount(
            @RequestParam Long postId
    ) {
        return ResponseEntity.ok(commentService.getListOfCommentAndCountReplyComment(postId));
    }
    @GetMapping("getCommentByPath")
    ResponseEntity<?> getCommentByPath(
            @RequestParam Long postId,
            @RequestParam String path
    ){
        return ResponseEntity.ok(
                commentService.getListOfCommentByPath(postId, path)
        );
    }

    @PatchMapping("/{commentId}/update")
    ResponseEntity<?> updateComment (
            @PathVariable Long commentId,
            @RequestBody UpdateCommentRequest request
    ) {
        return ResponseEntity.ok(commentService.updateComment(commentId, request));
    }

    @PatchMapping("/{commentId}")
    ResponseEntity<?> softDeletedComment(
            @PathVariable Long commentId
    ) {
        return ResponseEntity.ok(commentService.softDeletedComment(commentId));

    }

    @DeleteMapping("/{commentId}")
    ResponseEntity<?> hardDeletedComment(
            @PathVariable Long commentId
    ){
        return ResponseEntity.ok(commentService.hardDeletedComment(commentId));
    }


}
