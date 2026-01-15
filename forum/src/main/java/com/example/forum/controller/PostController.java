package com.example.forum.controller;

import com.example.forum.dto.request.CreatePostRequest;
import com.example.forum.dto.request.UpdatePostRequest;
import com.example.forum.dto.response.ApiResponse;
import com.example.forum.dto.response.PostResponseDto;
import com.example.forum.entity.UserEntity;
import com.example.forum.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/forum/posts")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
//cloud name: dbz9lqdui
// upload preset: uploadpresetname
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<?> createPost (
            @RequestBody CreatePostRequest request
            ) {
        PostResponseDto postResponse = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(
                        true,
                        "Create post successfully",
                        postResponse
                )
        );

    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "get post by id successfully",
                        postService.getPost(postId)
                )
        );
    }

    @GetMapping()
    public ResponseEntity<?> getPostByOwner(
            @RequestParam(required = true) Long userId,
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
            ){
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "get post by user successfully",
                        postService.getPostByUser(userId, keyword, pageable)
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<?> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirect,
            @RequestParam(defaultValue = "") String keyword
    ) {
        System.out.println("GET-getPosts");
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "get post by filter",
                        postService.getPosts(page, size, sortBy, sortDirect, keyword)
                )
        );
    }

    @PatchMapping("/{id}/update")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request
            ){
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Updated",
                postService.updatePost(id, request)
        ));
    }

    @PatchMapping("/{id}/soft-delete")
    public ResponseEntity<?> softDeletePost(@PathVariable Long id){
        postService.softDeletePost(id);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Post is temporaty deleted!",
               null
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> hardDeletePost(@PathVariable Long id){
        postService.hardDeletePost(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Post is permanently deleted",
                        null
                )
        );
    }
}
