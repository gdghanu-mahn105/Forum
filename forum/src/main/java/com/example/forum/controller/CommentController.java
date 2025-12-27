package com.example.forum.controller;


import com.example.forum.dto.request.CreateCommentRequest;
import com.example.forum.dto.request.UpdateCommentRequest;
import com.example.forum.dto.response.ApiResponse;
import com.example.forum.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
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
                .body( new ApiResponse<>(
                        true,
                        "Comment created",
                        commentService.createComment(postId,request)
                ));
    }
    @GetMapping("/getCommentCount")
    ResponseEntity<?> getCommentWithReplyCount(
            @RequestParam Long postId
    ) {
        return ResponseEntity.ok( new ApiResponse<>(
                true,
                "get successfully",
                commentService.getListOfCommentAndCountReplyComment(postId)
        ));
    }
    // for specific comment
    @GetMapping("/{postId}/getCommentCount")
    ResponseEntity<?> getCommentWithReplyCount(
            @PathVariable Long postId,
            @RequestParam String parentPath,
            @RequestParam Long parentId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "get successfully",
                        commentService.getListOfCommentAndCountReplyComment(postId,parentPath,parentId)
                )
        );
    }

    @GetMapping("/getPaginated")
    public ResponseEntity<?> getCommentsPaginated(
            @RequestParam Long postId, // Nhận postId

            // Thần kỳ: Spring Boot tự động nhận 'page', 'size', và 'sort'
            // và gom chúng vào một đối tượng 'Pageable'
            @PageableDefault(
                    size = 4, // Kích thước trang mặc định (giống code Nuxt)
                    sort = "createdAt", // Sắp xếp theo trường 'createdAt'
                    direction = Sort.Direction.DESC // Sắp xếp giảm dần
            ) Pageable pageable
    ) {
        // Chú ý: Code Supabase cũ có '.eq('order', 1)'
        // có thể nghĩa là chỉ lấy comment cấp 1 (top-level)
        // Bạn cần báo cho Service của bạn xử lý logic đó

        // Bạn sẽ cần tạo hàm 'getTopLevelComments' trong Service
        // để nhận 'postId' và 'pageable'
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Get comments successfully",
                        commentService.getTopLevelComments(postId, pageable) // <-- Sửa hàm service
                )
        );
    }

    @GetMapping("/getReplies/{parentId}")
    public ResponseEntity<?> getReplies(@PathVariable Long parentId) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Replies fetched successfully",
                        commentService.getReplies(parentId)
                )
        );
    }
    @GetMapping("getCommentByPath")
    ResponseEntity<?> getCommentByPath(
            @RequestParam Long postId,
            @RequestParam String path
    ){
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "get successfully",
                        commentService.getListOfCommentByPath(postId, path)
                )

        );
    }

    @PatchMapping("/{commentId}/update")
    ResponseEntity<?> updateComment (
            @PathVariable Long commentId,
            @RequestBody UpdateCommentRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Comment updated!",
                commentService.updateComment(commentId, request)
        ));
    }

    @PatchMapping("/{commentId}")
    ResponseEntity<?> softDeletedComment(
            @PathVariable Long commentId
    ) {
        commentService.softDeletedComment(commentId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Comment deleted!",
                        null
                )
        );

    }

    @DeleteMapping("/{commentId}")
    ResponseEntity<?> hardDeletedComment(
            @PathVariable Long commentId
    ){
        commentService.hardDeletedComment(commentId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Permanently deleted comment",
                        null
                )
        );
    }


}
