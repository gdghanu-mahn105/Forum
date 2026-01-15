package com.example.forum.service.impl;

import com.example.forum.common.constant.AppConstants;
import com.example.forum.service.CacheService;
import com.example.forum.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {
    private final CacheService redisService;

    @Value("${app.login.max-attempts}")
    private int maxLoginAttempt;

    @Value("${app.security.lock-duration}")
    private long lockTimeDuration;

    @Value("${app.security.fail-window}")
    private long failCountWindow;

    @Override
    public boolean isLocked(String email){
        return redisService.hasKey(AppConstants.PREFIX_LOCK_LOGIN+email);
    }

    @Override
    public void loginFail(String email){
        String failKey = AppConstants.PREFIX_FAIL_COUNT + email;
        String lockKey = AppConstants.PREFIX_LOCK_LOGIN + email;

        long attempts = redisService.increment(failKey);

        if (attempts == 1) {
            redisService.setExpire(failKey, failCountWindow, TimeUnit.SECONDS);
        }
        if (attempts >= maxLoginAttempt) {
            redisService.set(lockKey, AppConstants.REDIS_VALUE_LOCKED, lockTimeDuration, TimeUnit.SECONDS);

            redisService.delete(failKey);
        }

    }
    @Override
    public void loginSucceeded(String email) {
        redisService.delete(AppConstants.PREFIX_FAIL_COUNT + email);
        redisService.delete(AppConstants.PREFIX_LOCK_LOGIN + email);
    }
}
