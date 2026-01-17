package com.example.forum.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudinaryService {

    String uploadImage(MultipartFile file);

    List<String> uploadImages(List<MultipartFile> files);
}
