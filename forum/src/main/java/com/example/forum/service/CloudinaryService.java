package com.example.forum.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CloudinaryService {

    Map uploadImage(MultipartFile file);

    List<Map> uploadImages(List<MultipartFile> files);

    void deleteImage(String publicId);
}
