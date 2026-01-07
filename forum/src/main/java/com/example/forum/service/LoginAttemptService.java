package com.example.forum.service;

public interface LoginAttemptService {
    boolean isLocked(String email);
    void loginFail(String email);
    void loginSucceeded(String email);
}
