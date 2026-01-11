package com.example.forum.service;

import com.example.forum.dto.request.RegisterRequest;
import com.example.forum.dto.response.UserSummaryDto;

public interface AdminService {
    UserSummaryDto createAdmin(RegisterRequest request);
}
