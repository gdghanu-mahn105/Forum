package com.example.forum.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.forum.common.constant.AppConstants;
import com.example.forum.common.constant.MessageConstants;
import com.example.forum.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;


    @Override
    public String uploadImage(MultipartFile file) {
        try{
            validateFile(file);
            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = UUID.randomUUID()+"_"+originalFileName;

            Map params = ObjectUtils.asMap(
                    "public_id", uniqueFileName,
                    "folder", "forum_avatars",
                    "resource_type", "image", // Bắt buộc Cloudinary coi đây là ảnh. Nếu up file .exe nó sẽ lỗi ngay.
                    "allowed_formats", new String[]{"jpg", "png", "jpeg", "webp"},
                    "format", "jpg"
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            log.info("Image uploaded successfully: {}", uniqueFileName);

            return uploadResult.get("secure_url").toString();
        } catch (IOException e){

            log.error("Cloudinary upload error: {}", e.getMessage());
            throw new RuntimeException(MessageConstants.UPLOAD_FAILED, e);
        }
    }

    private void validateFile(MultipartFile file){

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(MessageConstants.FILE_EMPTY);
        }

        if (file.getSize() > AppConstants.MAX_FILE_SIZE) { // 5MB
            throw new RuntimeException(MessageConstants.FILE_TOO_LARGE);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        if (!AppConstants.ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new RuntimeException(MessageConstants.FILE_EXTENSION_NOT_SUPPORTED);
        }

        String contentType = file.getContentType();
        if (contentType == null || !AppConstants.ALLOWED_IMAGE_MIME_TYPES.contains(contentType)) {
            throw new RuntimeException(MessageConstants.FILE_NOT_VALID_IMAGE);
        }
    }
}
