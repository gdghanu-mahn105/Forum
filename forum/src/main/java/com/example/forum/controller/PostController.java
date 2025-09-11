package com.example.forum.controller;

import com.example.forum.dto.request.CreatePostRequest;
import com.example.forum.dto.request.UpdatePostRequest;
import com.example.forum.dto.response.PostResponseDto;
import com.example.forum.entity.UserEntity;
import com.example.forum.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forum/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<?> createPost (
            @RequestBody CreatePostRequest request,
            Authentication authentication
            ) {
        UserEntity user =(UserEntity) authentication.getPrincipal();
        Long userId = user.getUserId();
        PostResponseDto postResponse = postService.createPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(postResponse);

    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @GetMapping("/search")
    public ResponseEntity<?> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirect,
            @RequestParam(defaultValue = "") String keyword
    ) {
        return ResponseEntity.ok(postService.getPosts(page, size, sortBy, sortDirect, keyword));
    }

    @PatchMapping("/{id}/update")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request
            ){
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    @PatchMapping("/{id}/soft-delete")
    public ResponseEntity<?> softDeletePost(@PathVariable Long id){
        return ResponseEntity.ok(postService.softDeletePost(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> hardDeletePost(@PathVariable Long id){
        return ResponseEntity.ok(postService.hardDeletePost(id));
    }
}
