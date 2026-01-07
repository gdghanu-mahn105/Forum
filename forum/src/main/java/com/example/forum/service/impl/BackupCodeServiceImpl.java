package com.example.forum.service.impl;

import com.example.forum.entity.BackupCode;
import com.example.forum.entity.UserEntity;
import com.example.forum.repository.BackupCodeRepository;
import com.example.forum.service.BackupCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BackupCodeServiceImpl implements BackupCodeService {

    private final BackupCodeRepository backupCodeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<String> generateBackupCode(UserEntity user){
        backupCodeRepository.deleteByUserEntityUserId(user.getUserId());

        List<String> rawCodeList = new ArrayList<>();
        List<BackupCode> BackCodeEntities = new ArrayList<>();

        for (int i = 0; i < 10; i++){
            String rawCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            rawCodeList.add(rawCode);

            BackCodeEntities.add(BackupCode.builder()
                            .userEntity(user)
                            .codeHash(passwordEncoder.encode(rawCode))
                    .build());
        }

        backupCodeRepository.saveAll(BackCodeEntities);

        return rawCodeList;
    }
    @Override
    public boolean verifyBackupCode(UserEntity user, String code){
        List<BackupCode> BackupCodeEntities = backupCodeRepository.findByUserEntityUserId(user.getUserId());

        for (BackupCode backupCode : BackupCodeEntities) {
            if(passwordEncoder.matches(code,backupCode.getCodeHash() )){
                backupCodeRepository.delete(backupCode);
                return true;
            }
        }
        return false;
    }
}
