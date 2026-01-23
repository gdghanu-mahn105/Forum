package com.example.forum.service;

import com.example.forum.dto.response.UploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CloudinaryService {

    UploadResponseDto uploadImage(MultipartFile file);

    List<UploadResponseDto> uploadImages(List<MultipartFile> files);

    void deleteImage(String publicId);
}
