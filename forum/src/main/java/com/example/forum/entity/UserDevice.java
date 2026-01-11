package com.example.forum.entity;

import com.example.forum.entity.Enum.DeviceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_devices")
public class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String deviceId;
    private String deviceName;
    private String refreshTokenHash;
    private String lastIp;

    @Enumerated(EnumType.STRING)
    private DeviceStatus status; // Enum: ACTIVE, REVOKED, EXPIRED

    private Instant lastActiveAt;
    private Instant expiresAt;
}
