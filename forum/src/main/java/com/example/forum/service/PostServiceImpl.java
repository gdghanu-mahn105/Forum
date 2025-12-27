package com.example.forum.service;

import com.example.forum.dto.request.CreatePostRequest;
import com.example.forum.dto.request.UpdatePostRequest;
import com.example.forum.dto.response.*;
import com.example.forum.entity.*;
import com.example.forum.entity.Enum.EventType;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.repository.*;
import com.example.forum.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final TagRepository tagRepo;
    private final SecurityService securityService;
    private final NotificationService notificationService;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;

    @Override
    public PostResponseDto createPost(CreatePostRequest request) {

        UserEntity currentUser = securityService.getCurrentUser();  // dùng service
        Long userId = currentUser.getUserId();

        UserEntity creator = userRepo.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found!"));

//        Set<Category> categories = new HashSet<>(categoryRepo.findAllById(request.getCategoryIds()));
        Set<Tag> tags= new HashSet<>(tagRepo.findAllById(request.getTagIds()));

        PostEntity post = PostEntity.builder()
                .creator(creator)
//                .categories(categories)
                .postTitle(request.getPostTitle())
                .tags(tags)
                .postContent(request.getPostContent())
                .thumbnailUrl(request.getThumbnailUrl())
                .upvotes(0L)
                .downvotes(0L)
                .countedViews(0L)
                .isArchived(false)
                .build();


//        if(request.getMediaRequestList() != null && !request.getMediaRequestList().isEmpty()) {
//            Set<MediaEntity> mediaEntitySet = request.getMediaRequestList()
//                    .stream()
//                    .map(mediaRequest ->{
//                        MediaEntity mediaEntity = new MediaEntity();
//                        mediaEntity.setMediaType(mediaRequest.getType());
//                        mediaEntity.setUrl(mediaRequest.getUrl());
//                        mediaEntity.setSize(mediaRequest.getSize());
//                        mediaEntity.setPost(post);
//                        return mediaEntity;
//                    }).collect(Collectors.toSet());
//            post.setMediaFiles(mediaEntitySet);
//        }
        postRepo.save(post);

        NotificationEvent newNotificationEvent = notificationService.createEvent(
                EventType.NEW_POST,
                creator,
                request.getPostTitle(),
                post.getPostId(),
                "POST");

        notificationService.notifyFollowers(newNotificationEvent);


        return mapToPostResponseDto(post, null);
    }

    @Override
    public void removeMediaFromPost(Long postId, Long mediaId) {
        PostEntity post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        post.getMediaFiles().removeIf(media -> media.getId().equals(mediaId));
        // orphanRemoval = true => Hibernate sẽ xóa khỏi DB
        postRepo.save(post);
    }

    private PostResponseDto mapToPostResponseDto(PostEntity post, UserEntity currentUser) {

        Long commentCount = commentRepository.countByPostEntity(post);

        Integer timeRead =0;
        if(post.getPostContent()!=null && !post.getPostContent().isEmpty()){
            int words = post.getPostContent().split("\\s+").length;
            timeRead=(int) Math.ceil(words/150); // 200words/minute
        }
        // temporary false, null because of no auth
        String isVoted = null;
        Boolean isSaved = false;

        if (currentUser != null) {
            // 3. Logic kiểm tra isVoted
            Optional<Vote> voteOpt = VoteRepository.findByUserEntityAndPostEntity(currentUser, post);
            if (voteOpt.isPresent()) {
                isVoted = voteOpt.get().getVoteType().toString(); // "UPVOTE" hoặc "DOWNVOTE"
            }

            // 4. Logic kiểm tra isSaved (TODO: Bạn cần tạo SavePostRepository)
            // isSaved = savePostRepo.existsByUserEntityAndPostEntity(currentUser, post);
        }

        return PostResponseDto.builder()
                .postId(post.getPostId())
                .postTitle(post.getPostTitle())
                .postContent(post.getPostContent())
                .thumbnailUrl(post.getThumbnailUrl())
                .upvotes(post.getUpvotes())
                .downvotes(post.getDownvotes())
                .countedViews(post.getCountedViews())
//                .mediaEntityList(post.getMediaFiles()
//                        .stream().map(media -> MediaResponse.builder()
//                                .id(media.getId())
//                                .url(media.getUrl())
//                                .type(media.getMediaType())
//                                .size(media.getSize())
//                                .build())
//                        .collect(Collectors.toList())
//                )
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .creatorName(post.getCreator().displayUsername())
                .creatorId(post.getCreator().getUserId())
                .creatorAvatarUrl(post.getCreator().getAvatarUrl())
//                .categories(post
//                        .getCategories().stream()
//                        .map(this::mapToCategoryDto)
//                        .collect(Collectors.toSet())
//                )
                .tags(post.getTags().stream()
                        .map(this::mapToTagDto)
                        .collect(Collectors.toSet())
                )
                .commentCount(commentCount)
                .timeRead(timeRead)
                .isVoted(isVoted)
                .isSaved(isSaved)
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


    @Override
    public PostResponseDto getPost(Long postId) {
        PostEntity post= postRepo.findById(postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post not found!"));
        return mapToPostResponseDto(post, null);
    }

    @Override
    public PagedResponse<PostResponseDto> getPosts(int page, int size, String sortBy, String sortDirect, String keyword) {

        Sort sort = sortDirect.equalsIgnoreCase("asc")
                ? Sort.by(Sort.Direction.ASC, sortBy)
                : Sort.by(Sort.Direction.DESC, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword == null) {
            keyword = "";
        }
        UserEntity currentUser = securityService.getCurrentUserOrNull();

        Page<PostEntity> postEntitiesPage = postRepo.findByPostTitleContainingIgnoreCaseAndIsArchivedFalse(keyword, pageable);
        List<PostResponseDto> postListContent = postEntitiesPage.getContent().stream().map(postEntity -> mapToPostResponseDto(postEntity, currentUser)).toList();

        return new PagedResponse<>(
                postListContent,
                postEntitiesPage.getNumber(),
                postEntitiesPage.getSize(),
                postEntitiesPage.getTotalElements(),
                postEntitiesPage.getTotalPages(),
                postEntitiesPage.isLast()
        );
    }

    @Override
    public PostResponseDto updatePost(Long postId, UpdatePostRequest request) {

        PostEntity post = postRepo.findByPostId(postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post not found!"));

        if(post.getIsArchived()){
            throw new ResourceNotFoundException("Post not found!");
        }

        UserEntity currentUser = securityService.getCurrentUser();  // dùng service
        Long currentUserId = currentUser.getUserId();

        if(!post.getCreator().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not have permission to update this post");
        }

        if(request.getTitle() !=null && !request.getTitle().isBlank()) {
            post.setPostTitle(request.getTitle());
        }
        if(request.getContent() !=null && !request.getContent().isBlank()) {
            post.setPostContent(request.getContent());
        }

        if(request.getTagSet()!= null) {
            Set<Tag> tags = new HashSet<>(tagRepo.findAllById(request.getTagSet()));
            post.setTags(tags);
        }

        if (request.getMediaRequestList() != null) {
//            post.getMediaFiles().clear();

            Set<MediaEntity> newMediaSet = request.getMediaRequestList().stream()
                    .map(mediaRequest -> {
                        MediaEntity media = new MediaEntity();
                        media.setUrl(mediaRequest.getUrl());
                        media.setMediaType(mediaRequest.getType());
                        media.setPost(post);
                        return media;
                    }).collect(Collectors.toSet());

            post.getMediaFiles().addAll(newMediaSet);
        }
        postRepo.save(post);

        return mapToPostResponseDto(post, null);
    }



    @Override
    public void softDeletePost(Long id) {
        PostEntity post= postRepo.findByPostId(id)
                .orElseThrow(()-> new ResourceNotFoundException("Post not found!"));

        if(post.getIsArchived()){
            throw new ResourceNotFoundException("Post not found!");
        }

        UserEntity currentUser = securityService.getCurrentUser();  // dùng service
        Long currentUserId = currentUser.getUserId();

        if(!currentUserId.equals(post.getCreator().getUserId())) {
            throw new AccessDeniedException("You are not have permission to delete this post");
        }
        post.setIsArchived(true);
        postRepo.save(post);
    }

    @Override
    public void hardDeletePost(Long id) {
        PostEntity post= postRepo.findByPostId(id)
                .orElseThrow(()-> new ResourceNotFoundException("Post not found!"));
        postRepo.delete(post);
    }
}
