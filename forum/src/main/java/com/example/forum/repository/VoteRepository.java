package com.example.forum.repository;

import com.example.forum.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserEntityUserIdAndPostEntityPostId(Long userId, Long postId);
    long countByPostEntityPostIdAndValue (Long postId, int value);
}
