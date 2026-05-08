package com.careerpath.service;

import com.careerpath.dto.request.UpdateProfileRequest;
import com.careerpath.dto.response.StudentDashboardResponse;
import com.careerpath.dto.response.UserResponse;

public interface StudentService {

    StudentDashboardResponse getDashboard(Long studentId);

    UserResponse getProfile(Long studentId);

    UserResponse updateProfile(Long studentId, UpdateProfileRequest request);
}
