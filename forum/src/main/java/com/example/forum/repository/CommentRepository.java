package com.example.forum.repository;

import com.example.forum.dto.projection.CommentProjection;
import com.example.forum.entity.CommentEntity;
import com.example.forum.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    SELECT\s
       c.comment_id AS commentId,
       c.comment_content AS commentContent,
       c.comment_path AS commentPath,
       c.is_deleted AS isDeleted,
       c.created_at AS createdAt,
       c.updated_at AS updatedAt,
       c.post_id AS postId,
       u.user_id AS userId,
       u.user_name AS username,
       u.avatar_url AS avatarUrl,
       COUNT(r.comment_id) AS replyCount
   FROM comments c
   JOIN users u ON c.user_id = u.user_id
   LEFT JOIN comments r\s
          ON r.post_id = c.post_id
         AND r.comment_path LIKE CONCAT(c.comment_path, c.comment_id, '/%')
         AND r.comment_id <> c.comment_id
   WHERE c.post_id = :postId
     AND c.is_deleted = false
   GROUP BY c.comment_id, c.comment_content, c.comment_path,\s
            c.is_deleted, c.created_at, c.updated_at, c.post_id,
            u.user_id, u.user_name, u.avatar_url
   ORDER BY c.comment_path ASC;
""", nativeQuery = true)
    List<CommentProjection> findCommentsWithReplyCountByPostId(@Param("postId") Long postId);

    Optional<CommentEntity> findByCommentIdAndIsDeletedFalse(Long commentId);

    @Query(value = """
    SELECT
          c.comment_id AS commentId,
          c.comment_content AS commentContent,
          c.comment_path AS commentPath,
          c.is_deleted AS isDeleted,
          c.created_at AS createdAt,
          c.updated_at AS updatedAt,
          c.post_id AS postId,
          u.user_id AS userId,
          u.user_name AS username,
          u.avatar_url AS avatarUrl,
          COUNT(r.comment_id) AS replyCount
      FROM comments c
      JOIN users u ON c.user_id = u.user_id
      LEFT JOIN comments r
             ON r.post_id = c.post_id
            AND r.comment_path LIKE CONCAT(c.comment_path, c.comment_id, '/%')
            AND r.comment_id <> c.comment_id
      WHERE c.post_id = :postId
        AND c.comment_path like concat(:parentPath, :parentId ,'/')
        AND c.is_deleted = false
      GROUP BY c.comment_id, c.comment_content, c.comment_path,
               c.is_deleted, c.created_at, c.updated_at, c.post_id,
               u.user_id, u.user_name, u.avatar_url
      ORDER BY c.comment_path ASC;
""", nativeQuery = true)
    List<CommentProjection> findCommentsWithReplyCountByPostId(
            @Param("postId") Long postId,
            @Param("parentPath") String parentPath,
            @Param("parentId") Long parentId
    );

    Long countByPostEntity(PostEntity post);

    Page<CommentEntity> findByPostEntity_PostIdAndParentIdIsNull(Long postId, Pageable pageable);

    Long countByParentId(Long commentId);

    List<CommentEntity> findByParentIdOrderByCreatedAtAsc(Long parentId);
}
