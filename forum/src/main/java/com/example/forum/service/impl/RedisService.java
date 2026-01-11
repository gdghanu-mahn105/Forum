package com.example.forum.service.impl;

import com.example.forum.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService implements CacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void set(String key, Object value){
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit){
        redisTemplate.opsForValue().set(key,  value, timeout, unit);
    }

    @Override
    public Object get(String key){
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void delete(String key){
        redisTemplate.delete(key);
    }

    @Override
    public long increment(String key){
        return redisTemplate.opsForValue().increment(key);
    }

    @Override
    public void setExpire (String key, long timeout, TimeUnit unit){
        redisTemplate.expire(key, timeout, unit);
    }
}
