package com.example.forum.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class FollowId implements Serializable {

    private Long followerId;
    private Long followingId;

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if (!(o instanceof FollowId)) return false;
        FollowId that = (FollowId) o;
        return Objects.equals(followingId, that.followingId) && Objects.equals(followerId, that.followerId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(followingId, followerId);
    }
}
