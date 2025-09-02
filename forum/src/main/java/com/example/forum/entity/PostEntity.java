package com.example.forum.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private UserEntity creator;

    @ManyToMany
    @JoinTable(
            name = "post_categories",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @Column(name = "post_title")
    private String postTitle;

    @Column(name = "post_content")
    private String postContent;

    @Column(name = "thumbnail")
    private String thumbnailUrl;

    @Column(name = "upvotes")
    private Long upvotes;

    @Column(name = "downvotes")
    private Long downvotes;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "counted_views")
    private Long countedViews;

    @ManyToMany
    @JoinTable(name = "post_tags",
    joinColumns= @JoinColumn(name = "post_id"),
    inverseJoinColumns = @JoinColumn(name = "tag_id"),
    uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "tag_id"})
    )
    private Set<Tag> tags = new HashSet<>();


    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isDeleted = this.isDeleted != null ? this.isDeleted : false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
