package com.example.forum.auth;

import com.example.forum.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private final RedisService redisService;

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 900;
    private static final long FAIL_COUNT_WINDOW = 300;
    private static final String PREFIX_FAIL_COUNT = "login:fail:";
    private static final String PREFIX_LOCK_LOGIN = "login:locked:";

    public boolean isLocked(String email){
        return redisService.hasKey(PREFIX_LOCK_LOGIN+email);
    }

    public void loginFail(String email){
        String failKey = PREFIX_FAIL_COUNT + email;
        String lockKey = PREFIX_LOCK_LOGIN + email;

        long attempts = redisService.increment(failKey);

        if (attempts == 1) {
            redisService.setExpire(failKey, FAIL_COUNT_WINDOW, TimeUnit.SECONDS);
        }
        if (attempts > MAX_ATTEMPTS) {
            redisService.set(lockKey, "LOCKED", LOCK_TIME_DURATION, TimeUnit.SECONDS);

            redisService.delete(failKey);
        }

    }

    public void loginSucceeded(String email) {
        redisService.delete(PREFIX_FAIL_COUNT + email);
        redisService.delete(PREFIX_LOCK_LOGIN + email);
    }
}
