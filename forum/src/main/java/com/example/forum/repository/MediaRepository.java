package com.example.forum.repository;

import com.example.forum.entity.MediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<MediaEntity, Long> {
    List<MediaEntity> findByPostPostId(Long id);
}
