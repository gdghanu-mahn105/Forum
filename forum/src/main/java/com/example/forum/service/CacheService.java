package com.example.forum.service;

import java.util.concurrent.TimeUnit;

public interface CacheService {
    void set(String key, Object value);

    void set(String key, Object value, long timeout, TimeUnit unit);

    Object get(String key);

    boolean hasKey(String key);

    void delete(String key);

    long increment(String key);

    void setExpire (String key, long timeout, TimeUnit unit);
}
