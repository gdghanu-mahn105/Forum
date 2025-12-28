package com.example.forum.repository;

import com.example.forum.entity.UserDevice;
import com.example.forum.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    Optional<UserDevice> findByRefreshTokenHash(String hash);
    Optional<UserDevice> findByUserIdAndDeviceId(Long userId, String deviceId);
    boolean existsByUserIdAndDeviceId(Long userId, String deviceId);
}