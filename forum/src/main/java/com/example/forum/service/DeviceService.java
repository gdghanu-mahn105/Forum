package com.example.forum.service;

import com.example.forum.entity.UserEntity;

public interface DeviceService {
    boolean saveUserDevice(UserEntity user, String deviceId, String rawRefreshToken, String ua, String ip);
}
