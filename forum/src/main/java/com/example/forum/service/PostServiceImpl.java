package com.example.forum.service;

import com.example.forum.dto.request.CreatePostRequest;
import com.example.forum.dto.response.CategoryDto;
import com.example.forum.dto.response.PostResponseDto;
import com.example.forum.dto.response.TagDto;
import com.example.forum.dto.response.UserSummaryDto;
import com.example.forum.entity.Category;
import com.example.forum.entity.PostEntity;
import com.example.forum.entity.Tag;
import com.example.forum.entity.UserEntity;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.repository.CategoryRepository;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.TagRepository;
import com.example.forum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final TagRepository tagRepo;

    @Override
    public PostResponseDto createPost(CreatePostRequest request, Long userId) {
        UserEntity creator = userRepo.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found!"));

        Set<Category> categories = new HashSet<>(categoryRepo.findAllById(request.getCategoryIds()));
        Set<Tag> tags= new HashSet<>(tagRepo.findAllById(request.getTagIds()));

        PostEntity post = PostEntity.builder()
                .creator(creator)
                .categories(categories)
                .postTitle(request.getPostTitle())
                .tags(tags)
                .postContent(request.getPostContent())
                .thumbnailUrl(request.getThumbnailUrl())
                .upvotes(0L)
                .downvotes(0L)
                .countedViews(0L)
                .isDeleted(false)
                .build();
        postRepo.save(post);
        return mapToPostResponseDto(post);
    }

    private PostResponseDto mapToPostResponseDto(PostEntity post) {
        return PostResponseDto.builder()
                .postId(post.getPostId())
                .postTitle(post.getPostTitle())
                .postContent(post.getPostContent())
                .thumbnailUrl(post.getThumbnailUrl())
                .upvotes(post.getUpvotes())
                .downvotes(post.getDownvotes())
                .countedViews(post.getCountedViews())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .creator(mapToUserSummaryDto(post.getCreator()))
                .categories(post
                        .getCategories().stream()
                        .map(this::mapToCategoryDto)
                        .collect(Collectors.toSet())
                )
                .tags(post.getTags().stream()
                        .map(this::mapToTagDto)
                        .collect(Collectors.toSet())
                )
                .build();
    }
    private UserSummaryDto mapToUserSummaryDto(UserEntity user) {
        return UserSummaryDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
    private CategoryDto mapToCategoryDto(Category category) {
        return CategoryDto.builder()
                .categoryId(category.getCategoryId())      // map từ entity sang dto
                .categoryName(category.getCategoryName())
                .build();
    }

    private TagDto mapToTagDto(Tag tag) {
        return TagDto.builder()
                .tagId(tag.getTagId())
                .tagName(tag.getTagName())
                .build();
    }
}
