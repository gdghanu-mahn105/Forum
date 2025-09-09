package com.example.forum.service;

import com.example.forum.dto.projection.CommentProjection;
import com.example.forum.dto.request.CreateCommentRequest;
import com.example.forum.dto.request.UpdateCommentRequest;
import com.example.forum.dto.response.ApiResponse;
import com.example.forum.dto.response.CommentDto;
import com.example.forum.dto.response.UserSummaryDto;
import com.example.forum.entity.CommentEntity;
import com.example.forum.entity.PostEntity;
import com.example.forum.entity.UserEntity;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final PostRepository postRepository;
    private final SecurityService securityService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Override
    public CommentDto createComment(Long postId, CreateCommentRequest request) {

        PostEntity post= postRepository.findByPostId(postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post not found!"));

        UserEntity currentUser = securityService.getCurrentUser();
        UserEntity user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(()-> new ResourceNotFoundException("User not found!"));

        CommentEntity comment= CommentEntity.builder()
                .postEntity(post)
                .userEntity(user)
                .commentContent(request.getContent())
                .isDeleted(false)
                .build();

        String newPath;
        if(request.getParentPath() == null
                || request.getParentPath().trim().isEmpty()
                || request.getParentId()==0
        ){
            newPath= "/";
            comment.setParentId(null);
        } else {
            comment.setParentId(request.getParentId());
            newPath = request.getParentPath() + request.getParentId() +"/";
        }
        comment.setCommentPath(newPath);
        commentRepository.save(comment);

        return mapToCommentDto(comment);
    }

    @Override
    public List<CommentDto> getListOfCommentByPath(Long postId, String path) {
        List<CommentEntity> comments = commentRepository.findByPostIdAndPathLike(postId, path);

        return comments.stream()
                .map(this::mapToCommentDto)
                .toList();
    }

    @Override
    public List<CommentDto> getListOfCommentAndCountReplyComment(Long postId) {

        PostEntity post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found!"));

        List<CommentProjection> rows = commentRepository.findCommentsWithReplyCountByPostId(postId);

        return rows.stream()
                .map(row -> CommentDto.builder()
                        .commentId(row.getCommentId())
                        .commentContent(row.getCommentContent())
                        .commentPath(row.getCommentPath())
                        .isDeleted(row.getIsDeleted())
                        .createdAt(row.getCreatedAt())
                        .updatedAt(row.getUpdatedAt())
                        .userInfor(new UserSummaryDto(
                                row.getUserId(),
                                row.getUsername(),
                                row.getEmail(),
                                row.getAvatarUrl()
                        ))
                        .post(new CommentDto.PostInfo(post.getPostId(), post.getPostTitle()))
                        .replyCount(row.getReplyCount())//reply count
                        .build()
                )
                .toList();
    }

    @Override
    public CommentDto updateComment(Long commentId, UpdateCommentRequest request) {
        CommentEntity comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(()-> new ResourceNotFoundException("Comment not found!"));

        UserEntity currentUser = securityService.getCurrentUser();
        Long currentUserId = currentUser.getUserId();

        if(currentUserId != comment.getUserEntity().getUserId()) {
            throw new AccessDeniedException("You can only edit your own comment!");
        }

        comment.setCommentContent(request.getUpdatedContent());
        commentRepository.save(comment);

        return mapToCommentDto(comment);
    }

    @Override
    public ApiResponse<?> softDeletedComment(Long commentId) {
        CommentEntity comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(()-> new ResourceNotFoundException("Comment not found!"));

        UserEntity currentUser = securityService.getCurrentUser();
        Long currentUserId = currentUser.getUserId();

        if(currentUserId != comment.getUserEntity().getUserId()) {
            throw new AccessDeniedException("You can only delete your own comment!");
        }
        comment.setIsDeleted(true);
        commentRepository.save(comment);
        return ApiResponse.builder()
                .success(true)
                .message("Deleted")
                .build();
    }

    @Override
    public ApiResponse<?> hardDeletedComment(Long commentId) {
        CommentEntity comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(()-> new ResourceNotFoundException("Comment not found!"));

        UserEntity currentUser = securityService.getCurrentUser();
        Long currentUserId = currentUser.getUserId();

        if(currentUserId != comment.getUserEntity().getUserId()) {
            throw new AccessDeniedException("You can only delete your own comment!");
        }
        commentRepository.delete(comment);
        return ApiResponse.builder()
                .success(true)
                .message("Permanently deleted")
                .build();
    }

    @Override
    public List<CommentDto> getListOfCommentAndCountReplyComment(Long postId, String parentPath, Long parentId) {
        PostEntity post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found!"));

        List<CommentProjection> rows = commentRepository.findCommentsWithReplyCountByPostId(postId, parentPath, parentId);

        return rows.stream()
                .map(row -> CommentDto.builder()
                        .commentId(row.getCommentId())
                        .commentContent(row.getCommentContent())
                        .commentPath(row.getCommentPath())
                        .isDeleted(row.getIsDeleted())
                        .createdAt(row.getCreatedAt())
                        .updatedAt(row.getUpdatedAt())
                        .userInfor(new UserSummaryDto(
                                row.getUserId(),
                                row.getUsername(),
                                row.getEmail(),
                                row.getAvatarUrl()
                        ))
                        .post(new CommentDto.PostInfo(post.getPostId(), post.getPostTitle()))
                        .replyCount(row.getReplyCount())//reply count
                        .build()
                )
                .toList();
    }


    private CommentDto mapToCommentDto(CommentEntity comment){
        return CommentDto.builder()
                .commentId(comment.getCommentId())
                .commentContent(comment.getCommentContent())
                .commentPath(comment.getCommentPath())
//                .likes(com)
                .isDeleted(comment.getIsDeleted())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replyCount(0L)
                .userInfor(mapToUserSummary(comment.getUserEntity()))
                .post( new CommentDto.PostInfo(
                        comment.getPostEntity().getPostId(),
                        comment.getPostEntity().getPostTitle()
                ))
                .build();
    }
    private UserSummaryDto mapToUserSummary(UserEntity user){
        return UserSummaryDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.displayUsername())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

}

