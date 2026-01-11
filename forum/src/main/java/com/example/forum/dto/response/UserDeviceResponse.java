package com.example.forum.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserDeviceResponse {
    private String deviceId;
    private String deviceName;
    private String lastIp;
    private Instant lastActiveAt;
    private boolean isCurrentDevice;
}
