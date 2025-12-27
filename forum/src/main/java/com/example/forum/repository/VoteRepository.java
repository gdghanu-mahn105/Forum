package com.example.forum.repository;

import com.example.forum.dto.projection.VoteProjection;
import com.example.forum.entity.PostEntity;
import com.example.forum.entity.UserEntity;
import com.example.forum.entity.Vote;
import com.example.forum.entity.Enum.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    static Optional<Vote> findByUserEntityAndPostEntity(UserEntity currentUser, PostEntity post){
        return Optional.empty();
    }

    Optional<Vote> findByUserEntityUserIdAndPostEntityPostId(Long userId, Long postId);
    long countByPostEntityPostIdAndVoteType (Long postId, VoteType voteType);

    @Query(value = """
        SELECT
            v.user_id AS userId,
            u.user_name AS username,
            u.avatar_url AS avatarUrl,
            v.vote_type AS voteType
        FROM votes v
        JOIN users u ON u.user_id = v.user_id
        WHERE v.post_id = :postId and v.vote_type = :voteType
    """, nativeQuery = true)
    List<VoteProjection> findVotesOfPost(
            @Param("postId") Long postId,
            @Param("voteType")String voteType
            );
}
