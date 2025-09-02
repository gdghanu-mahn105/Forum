package com.example.forum.repository;

import com.example.forum.dto.projection.CommentProjection;
import com.example.forum.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    @Query(value = "SELECT * FROM comments where post_id=:postId AND comment_path LIKE :path order by created_at",
    nativeQuery = true)
    List<CommentEntity> findByPostIdAndPathLike(
            @Param("postId") Long postId,
            @Param("path") String path
    );

    @Query(value = """
    SELECT 
        c.comment_id AS commentId,
        c.comment_content AS commentContent,
        c.comment_path AS commentPath,
        c.is_deleted as isDeleted,
        c.created_at as createdAt,
        c.updated_at as updatedAt,
        c.post_id AS postId,
        u.user_id AS userId,
        u.user_name AS username,
        u.avatar_url AS avatarUrl,
        (
            SELECT COUNT(*) 
            FROM comments r
            WHERE r.post_id = c.post_id
              AND r.comment_path LIKE CONCAT(c.comment_path, c.comment_id, '/%')
              AND r.comment_id <> c.comment_id
        ) AS replyCount
    FROM comments c
    JOIN users u ON c.user_id= u.user_id
    WHERE c.post_id = :postId
    AND c.is_deleted = false
    ORDER BY c.comment_path ASC
""", nativeQuery = true)
    List<CommentProjection> findCommentsWithReplyCountByPostId(@Param("postId") Long postId);

    Optional<CommentEntity> findByCommentIdAndIsDeletedFalse(Long commentId);
}
