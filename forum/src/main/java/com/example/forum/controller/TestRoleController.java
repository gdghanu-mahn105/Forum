package com.example.forum.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRoleController {
    @GetMapping("/forum/admin/dashboard")
    public String adminDashboard() {
        return "Hello Admin! Đây là dashboard chỉ admin mới xem được.";
    }

    @GetMapping("/forum/user/profile")
    public String userProfile() {
        return "Hello User! Đây là trang profile của bạn.";
    }
}
