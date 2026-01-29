package com.example.forum.service.impl;

import com.example.forum.entity.Enum.DeviceStatus;
import com.example.forum.entity.UserDevice;
import com.example.forum.entity.UserEntity;
import com.example.forum.repository.UserDeviceRepository;
import com.example.forum.security.jwt.TokenUtils;
import com.example.forum.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    @Value("${app.refresh-token.expiration}")
    private long refreshTokenExpirationTime;

    private final UserDeviceRepository userDeviceRepository;

    @Override
    public boolean saveUserDevice(UserEntity user, String deviceId, String rawRefreshToken, String ua, String ip){

        var existingDevice = userDeviceRepository.findByUserIdAndDeviceId(user.getUserId(), deviceId);
        boolean isNewDevice = existingDevice.isEmpty();

        UserDevice userDevice = existingDevice.orElse(
                UserDevice.builder()
                        .userId(user.getUserId())
                        .deviceId(deviceId)
                        .build());

        if (ua != null && ua.length() > 255) {
            ua = ua.substring(0, 255);
        }
        userDevice.setRefreshTokenHash(TokenUtils.hashToken(rawRefreshToken));
        userDevice.setDeviceName(ua); // Tạm thời lưu user-agent, sau này parse đẹp sau
        userDevice.setLastIp(ip);
        userDevice.setStatus(DeviceStatus.ACTIVE);
        userDevice.setLastActiveAt(Instant.now());
        userDevice.setExpiresAt(Instant.now().plus(refreshTokenExpirationTime, ChronoUnit.MILLIS));
        userDeviceRepository.save(userDevice);

        return isNewDevice;
    }
}
