package com.example.forum.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.forum.common.constant.AppConstants;
import com.example.forum.common.constant.MessageConstants;
import com.example.forum.core.exception.BadRequestException;
import com.example.forum.dto.response.UploadResponseDto;
import com.example.forum.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${app.upload.max-file-size}")
    private long maxFileSize;

    @Value("${app.upload.max-batch-size}")
    private long maxBatchSize;

    @Override
    public UploadResponseDto uploadImage(MultipartFile file) {
        try{
            validateFile(file);
            String originalFileName = file.getOriginalFilename();
            String fileNameWithoutExtension = originalFileName;
            if (originalFileName != null && originalFileName.contains(".")) {
                fileNameWithoutExtension = originalFileName.substring(0, originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID()+"_"+fileNameWithoutExtension;

            Map params = ObjectUtils.asMap(
                    "public_id", uniqueFileName,
                    "folder", AppConstants.CLOUDINARY_FOLDER_AVATARS,
                    "resource_type", "image", // Bắt buộc Cloudinary coi đây là ảnh. Nếu up file .exe nó sẽ lỗi ngay.
                    "allowed_formats", new String[]{"jpg", "png", "jpeg", "webp"},
                    "format", "jpg"
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            log.info("Image uploaded successfully: {}", uniqueFileName);

            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            String format = (String) uploadResult.get("format");
            String resource_type =(String) uploadResult.get("resource_type");
            Long bytes =(Long.parseLong(uploadResult.get("bytes").toString()));

            return UploadResponseDto.builder()
                    .id(publicId)
                    .url(secureUrl)
                    .format(format)
                    .resourceType(resource_type)
                    .bytes(bytes)
                    .build();
        } catch (IOException e){

            log.error("Cloudinary upload error: {}", e.getMessage());
            throw new RuntimeException(MessageConstants.UPLOAD_FAILED, e);
        }
    }

    @Override
    public List<UploadResponseDto> uploadImages(List<MultipartFile> files) {
        if(files == null || files.isEmpty()){
            throw new BadRequestException(MessageConstants.FILE_EMPTY);
        }
        if(files.size()>maxFileSize){
            throw new BadRequestException(MessageConstants.MAX_BATCH_SIZE_EXCEEDED);
        }

        log.info("Starting batch upload for {} files...", files.size());

        return files.parallelStream()
                .map(this::uploadImage) // Tái sử dụng logic upload đơn lẻ
                .collect(Collectors.toList());
    }

    @Async
    @Override
    public void deleteImage(String publicId) {
        try{
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e){
            log.error("Error when deleting media: {}", e.getMessage());
        }
    }


    private void validateFile(MultipartFile file){

        if (file == null || file.isEmpty()) {
            throw new BadRequestException(MessageConstants.FILE_EMPTY);
        }

        if (file.getSize() > maxFileSize) { // 5MB
            throw new BadRequestException(MessageConstants.FILE_TOO_LARGE);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        if (!AppConstants.ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BadRequestException(MessageConstants.FILE_EXTENSION_NOT_SUPPORTED);
        }

        String contentType = file.getContentType();
        if (contentType == null || !AppConstants.ALLOWED_IMAGE_MIME_TYPES.contains(contentType)) {
            throw new BadRequestException(MessageConstants.FILE_NOT_VALID_IMAGE);
        }
    }
}
