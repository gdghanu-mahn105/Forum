package com.example.forum.service;

import com.example.forum.entity.UserEntity;

import java.util.List;

public interface BackupCodeService {
    List<String> generateBackupCode(UserEntity user);
    boolean verifyBackupCode(UserEntity user, String code);
}
