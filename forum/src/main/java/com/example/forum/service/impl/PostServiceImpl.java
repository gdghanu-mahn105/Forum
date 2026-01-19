package com.example.forum.service.impl;

import com.example.forum.common.constant.MessageConstants;
import com.example.forum.dto.request.CreatePostRequest;
import com.example.forum.dto.request.UpdatePostRequest;
import com.example.forum.dto.response.*;
import com.example.forum.entity.*;
import com.example.forum.entity.Enum.EventType;
import com.example.forum.core.exception.ResourceNotFoundException;
import com.example.forum.entity.Enum.MediaType;
import com.example.forum.repository.*;
import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.service.CloudinaryService;
import com.example.forum.service.NotificationService;
import com.example.forum.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final TagRepository tagRepo;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final MediaRepository mediaRepository;

    private final SecurityUtils securityService;
    private final NotificationService notificationService;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public PostResponseDto createPost(CreatePostRequest request, List<MultipartFile> files) {

        UserEntity currentUser = securityService.getCurrentUser();  // dùng service
        Long userId = currentUser.getUserId();

        UserEntity creator = userRepo.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

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


        postRepo.save(post);

        if(files !=null && !files.isEmpty()){
            List<Map> mediaInfo = cloudinaryService.uploadImages(files);

            saveMediaEntity(mediaInfo, post);
        }

        NotificationEvent newNotificationEvent = notificationService.createEvent(
                EventType.NEW_POST,
                creator,
                request.getPostTitle(),
                post.getPostId(),
                "POST");

        notificationService.notifyFollowers(newNotificationEvent);

        return mapToPostResponseDto(post, currentUser);
    }

    @Override
    public void removeMediaFromPost(Long postId, Long mediaId) {
        PostEntity post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));

        UserEntity currentUser = securityService.getCurrentUser();
        if (!post.getCreator().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException(MessageConstants.NO_PERMISSION_TO_DELETE_MEDIA);
        }

        MediaEntity mediaEntity = mediaRepository.findById(mediaId)
                        .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.MEDIA_NOT_FOUND));

        if (!mediaEntity.getPost().getPostId().equals(postId)) {
            throw new IllegalArgumentException(MessageConstants.MEDIA_NOT_BELONG_TO_POST);
        }

        String publicIdToDelete = mediaEntity.getPublicId();

        mediaRepository.delete(mediaEntity);

        if (publicIdToDelete != null) {
            cloudinaryService.deleteImage(publicIdToDelete);
        }
    }

    public void saveMediaEntity(List<Map> mediaList, PostEntity post){
        Set<MediaEntity> mediaEntitySet = mediaList.stream().map(media ->{
            String resourceType = media.get("resource_type").toString();
            MediaEntity mediaEntity = new MediaEntity();
            mediaEntity.setPost(post);
            mediaEntity.setMediaType(resourceType.equals("image") ? MediaType.IMAGE : MediaType.VIDEO);
            mediaEntity.setPublicId(media.get("public_id").toString());
            mediaEntity.setUrl(media.get("secure_url").toString());
            if (media.get("bytes") != null) {
                long sizeInBytes = Long.parseLong(media.get("bytes").toString());
                mediaEntity.setSize(sizeInBytes);
            }
            return  mediaEntity;
        }).collect(Collectors.toSet());
        mediaRepository.saveAll(mediaEntitySet);
    }

    @Override
    @Transactional
    public PostResponseDto addMediaToPost(Long postId, List<MultipartFile> files) {
        PostEntity post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));

        UserEntity currentUser = securityService.getCurrentUser();
        if (!post.getCreator().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException(MessageConstants.NO_PERMISSION_EDIT_POST);
        }

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException(MessageConstants.FILE_EMPTY);
        }

        List<Map> mediaInfo = cloudinaryService.uploadImages(files);

        saveMediaEntity(mediaInfo, post);

        return mapToPostResponseDto(post, currentUser);
    }

    private PostResponseDto mapToPostResponseDto(PostEntity post, UserEntity currentUser) {

        Long commentCount = commentRepository.countByPostEntity(post);

        List<MediaEntity> mediaEntityList = mediaRepository.findByPostPostId(post.getPostId());

        Integer timeRead =0;
        if (post.getPostContent() != null && !post.getPostContent().isEmpty()) {
            int words = post.getPostContent().split("\\s+").length;
            timeRead = (int) Math.ceil((double) words / 150);
        }

        String isVoted = null;
        Boolean isSaved = false;

        if (currentUser != null) {
            Optional<Vote> voteOpt = voteRepository.findByUserEntityUserIdAndPostEntityPostId(currentUser.getUserId(), post.getPostId());
            if (voteOpt.isPresent()) {
                isVoted = voteOpt.get().getVoteType().toString();
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
                .mediaEntityList( mediaEntityList)
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
        UserEntity currentUser = securityService.getCurrentUser();
        PostEntity post= postRepo.findById(postId)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));
        return mapToPostResponseDto(post, currentUser);
    }

    @Override
    public PagedResponse<PostResponseDto> getPostByUser(Long userId, String keyword, Pageable pageable) {
        if (keyword == null) {
            keyword = "";
        }
        UserEntity owner = userRepo.findById(userId).orElseThrow(()->new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        UserEntity currentUser = securityService.getCurrentUserOrNull();

        Page<PostEntity> postEntitiesPage = postRepo.findByCreatorUserIdAndIsArchivedFalseAndPostTitleContainingIgnoreCase(userId,keyword, pageable);
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
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));

        if(post.getIsArchived()){
            throw new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND);
        }

        UserEntity currentUser = securityService.getCurrentUser();  // dùng service
        Long currentUserId = currentUser.getUserId();

        if(!post.getCreator().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException(MessageConstants.NO_PERMISSION_EDIT_POST);
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

        postRepo.save(post);

        return mapToPostResponseDto(post, currentUser);
    }



    @Override
    public void softDeletePost(Long id) {
        PostEntity post= postRepo.findByPostId(id)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));

        if(post.getIsArchived()){
            throw new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND);
        }

        UserEntity currentUser = securityService.getCurrentUser();
        Long currentUserId = currentUser.getUserId();

        if(!currentUserId.equals(post.getCreator().getUserId())) {
            throw new AccessDeniedException(MessageConstants.NO_PERMISSION_EDIT_POST);
        }
        post.setIsArchived(true);
        postRepo.save(post);
    }

    @Override
    public void hardDeletePost(Long id) {
        PostEntity post= postRepo.findByPostId(id)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));
        postRepo.delete(post);
    }
}
