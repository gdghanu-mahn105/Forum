package com.example.forum.repository;

import com.example.forum.entity.BackupCode;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackupCodeRepository extends JpaRepository<BackupCode, Long> {

    void deleteByUserEntityUserId(Long id);

    List<BackupCode> findByUserEntityUserId(Long userId);
}
