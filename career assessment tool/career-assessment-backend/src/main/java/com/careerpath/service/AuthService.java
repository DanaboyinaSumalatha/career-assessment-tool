package com.careerpath.service;

import com.careerpath.dto.request.LoginRequest;
import com.careerpath.dto.request.RegisterRequest;
import com.careerpath.dto.response.AuthResponse;
import com.careerpath.dto.response.UserResponse;
import com.careerpath.model.User;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse adminLogin(LoginRequest request);

    UserResponse getProfile(Long userId);

    User getCurrentUser(String email);
}
