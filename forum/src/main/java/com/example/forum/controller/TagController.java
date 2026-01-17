package com.example.forum.controller;

import com.example.forum.entity.Tag; // Import Entity
import com.example.forum.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/forum/tags")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() { // <-- Sửa ở đây
        List<Tag> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }
}