package com.example.forum.repository;

import com.example.forum.dto.projection.UserSummaryProjection;
import com.example.forum.entity.Follow;
import com.example.forum.entity.FollowId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    @Query(
        value = """
            select
            u.user_id as "userId",
            u.user_name as "userName",
            u.email as "email",
            u.avatar_url as "avatarUrl"
            from follow f join users u
            on f.follower_id = u.user_id
            where f.following_id = :followingId
            and u.user_name like concat('%', :username, '%')
            order by u.user_name asc
        """,
        countQuery = """
            select count(*)
            from follow f
            join users u on f.follower_id = u.user_id
            where f.following_id = :followingId
            and u.user_name like concat('%', :username, '%')
        """,
            nativeQuery = true
    )
    Page<UserSummaryProjection> findFollowerByFollowingIdAndUsername(
            @Param("followingId") Long followingId,
            @Param("username") String searchParam,
            Pageable pageable
    );


    @Query(
            value = """
            select
            u.user_id as "userId",
            u.user_name as "userName",
            u.email as "email",
            u.avatar_url as "avatarUrl"
            from follow f join users u
            on f.following_id = u.user_id
            where f.follower_id = :followerId
            and u.user_name like concat('%', :username, '%')
            order by u.user_name asc
        """,
            countQuery = """
            select count(*)
            from follow f
            join users u on f.following_id = u.user_id
            where f.follower_id = :followerId
            and u.user_name like concat('%', :username, '%')
        """,
            nativeQuery = true
    )
    Page<UserSummaryProjection> findFollowingUserIdByFollowerUserId(
            @Param("followerId") Long followerId,
            @Param("username") String searchParam,
            Pageable pageable
    );
    boolean existsById(FollowId id);

    @Query("""
        select count(f)
        from Follow f
        where f.following.id = :followingId
    """)
    int countFollowers(@Param("followingId") Long followingId);

    @Query("""
        select count(f)
        from Follow f
        where f.follower.id = :followerId
    """)
    int countFollowings(@Param("followerId") Long followerId);

    @Query("SELECT f.follower.userId FROM Follow f WHERE f.following.userId = :followingId")
    List<Long> findFollowerUserIdByFollowingUserId(Long followingId);
}
