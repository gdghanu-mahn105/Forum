package com.example.forum.controller;

import com.example.forum.dto.request.CreatePostRequest;
import com.example.forum.dto.request.UpdatePostRequest;
import com.example.forum.dto.response.ApiResponse;
import com.example.forum.dto.response.PostResponseDto;
import com.example.forum.entity.UserEntity;
import com.example.forum.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/forum/posts")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(value ="/create")
    public ResponseEntity<?> createPost (
            @Valid @RequestBody CreatePostRequest request
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

    @PostMapping(value = "/{postId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addMediaToPost(
            @PathVariable Long postId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        PostResponseDto updatedPost = postService.addMediaToPost(postId, files);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Added media successfully", updatedPost)
        );
    }

    @DeleteMapping("/{postId}/media/{mediaId}")
    public ResponseEntity<?> removeMediaFromPost(
            @PathVariable Long postId,
            @PathVariable Long mediaId
    ) {
        postService.removeMediaFromPost(postId, mediaId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Remove media successfully",
                        null
                )
        );
    }
}
