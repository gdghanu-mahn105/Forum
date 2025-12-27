package com.example.forum.service;

import com.example.forum.dto.projection.CommentProjection;
import com.example.forum.dto.request.CreateCommentRequest;
import com.example.forum.dto.request.UpdateCommentRequest;
import com.example.forum.dto.response.*;
import com.example.forum.entity.CommentEntity;
import com.example.forum.entity.Enum.EventType;
import com.example.forum.entity.NotificationEvent;
import com.example.forum.entity.PostEntity;
import com.example.forum.entity.UserEntity;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final PostRepository postRepository;
    private final SecurityService securityService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    @Override
    public CommentResponseDto createComment(Long postId, CreateCommentRequest request) {

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
            CommentEntity parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(()-> new ResourceNotFoundException("Comment not found!"));
            comment.setParentId(request.getParentId());
            newPath = request.getParentPath() + request.getParentId() +"/";
        }
        comment.setCommentPath(newPath);
        commentRepository.save(comment);

        // Notification
        if(request.getParentPath() == null
                || request.getParentPath().trim().isEmpty()
                || request.getParentId()==0) {
            NotificationEvent notificationEvent = notificationService.createEvent(
                    EventType.NEW_COMMENT,
                    currentUser,
                    request.getContent().substring(0, 20),
                    comment.getCommentId(),
                    "COMMENT"
            );
            notificationService.notifySpecificUser(post.getCreator(), notificationEvent);
        } else {
            NotificationEvent notificationEvent = notificationService.createEvent(
                    EventType.NEW_REPLY,
                    currentUser,
                    request.getContent().substring(0, 20),
                    comment.getCommentId(),
                    "COMMENT"
            );
            CommentEntity parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(()-> new ResourceNotFoundException("Comment not found!"));
            notificationService.notifySpecificUser(post.getCreator(), notificationEvent);
            notificationService.notifySpecificUser(parentComment.getUserEntity(), notificationEvent);

        }


        return mapToCommentResponseDto(comment);
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
    public void softDeletedComment(Long commentId) {
        CommentEntity comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(()-> new ResourceNotFoundException("Comment not found!"));

        UserEntity currentUser = securityService.getCurrentUser();
        Long currentUserId = currentUser.getUserId();

        if(currentUserId != comment.getUserEntity().getUserId()) {
            throw new AccessDeniedException("You can only delete your own comment!");
        }
        comment.setIsDeleted(true);
        commentRepository.save(comment);
    }

    @Override
    public void hardDeletedComment(Long commentId) {
        CommentEntity comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(()-> new ResourceNotFoundException("Comment not found!"));

        UserEntity currentUser = securityService.getCurrentUser();
        Long currentUserId = currentUser.getUserId();

        if(currentUserId != comment.getUserEntity().getUserId()) {
            throw new AccessDeniedException("You can only delete your own comment!");
        }
        commentRepository.delete(comment);
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

    @Override
    public PagedResponse<CommentResponseDto> getTopLevelComments(Long postId, Pageable pageable) {
        PostEntity post = postRepository.findByPostId(postId).orElseThrow(()-> new ResourceNotFoundException("Post not found!"));
        Page<CommentEntity> commentPage = commentRepository
                .findByPostEntity_PostIdAndParentIdIsNull(postId, pageable);


        Page<CommentResponseDto> dtoPage = commentPage.map(this::mapToCommentResponseDto);

        // 3. Tạo và trả về đối tượng PagedResponse
        // (Giả sử class PagedResponse của bạn có constructor như sau)
        return new PagedResponse<>(
                dtoPage.getContent(),      // List<CommentResponseDto>
                dtoPage.getNumber(),       // Số trang hiện tại
                dtoPage.getSize(),         // Kích thước trang
                dtoPage.getTotalElements(),// Tổng số comment (cấp 1)
                dtoPage.getTotalPages(),   // Tổng số trang
                dtoPage.isLast()           // Trang cuối?
        );
    }


    @Override
    public List<CommentResponseDto> getReplies(Long parentId) {
        if (!commentRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Parent comment not found!");
        }

        List<CommentEntity> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(parentId);

        return replies.stream()
                .map(this::mapToCommentResponseDto)
                .toList();
    }

    private CommentResponseDto mapToCommentResponseDto(CommentEntity comment){
        if (comment == null) return null;
        String path = comment.getCommentPath();
        String parentPath = null;
        int depth = 0;

        if (path != null && !path.isEmpty()) {
            // depth = số lượng dấu '/'
            depth = StringUtils.countMatches(path, "/");

            // Nếu depth > 1 (ví dụ: "/123/"), parentPath là null
            // Nếu depth > 2 (ví dụ: "/123/456/"), parentPath là "/123/"
            if (depth > 1) {
                // Tìm vị trí dấu '/' thứ 2 từ cuối lên
                int lastSlash = path.lastIndexOf('/', path.length() - 2);
                if (lastSlash >= 0) {
                    parentPath = path.substring(0, lastSlash + 1);
                }
            }
        }
        UserEntity user = comment.getUserEntity();

        return CommentResponseDto.builder()
                .id(comment.getCommentId())
                .postId(comment.getPostEntity().getPostId())
                .ownerId(user != null ? user.getUserId() : null)

                .content(comment.getCommentContent())
                .isArchived(comment.getIsDeleted())

                .createdAt(comment.getCreatedAt() != null ? comment.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(comment.getUpdatedAt() != null ? comment.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)

                .path(path)
                .depth(depth)
                .parentPath(parentPath)
                .childCommentCount(commentRepository.countByParentId(comment.getCommentId())) // Tạm thời set 0
                .children(Collections.emptyList()) // Luôn trả về mảng rỗng

                .upvote(0L)
                .downvote(0L)
                .userVoteType(null)

                .owner(mapToOwnerCommentDto(user))
                .build();
    }

    private CommentOwnerDto mapToOwnerCommentDto(UserEntity owner){
        if (owner == null) return null;

        return CommentOwnerDto.builder()
                .id(owner.getUserId())
                .name(owner.displayUsername()) // Dùng hàm có sẵn
                .photo(owner.getAvatarUrl()) // Dùng hàm có sẵn

                .createdAt(owner.getCreatedAt() != null ? owner.getCreatedAt().atOffset(ZoneOffset.UTC) : null)

                .slug(owner.getSlug())
                .point(null)
                .bio(owner.getBio())
                .build();
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

