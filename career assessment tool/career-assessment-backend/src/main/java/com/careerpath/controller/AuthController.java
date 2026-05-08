package com.careerpath.controller;

import com.careerpath.dto.request.LoginRequest;
import com.careerpath.dto.request.RegisterRequest;
import com.careerpath.dto.response.ApiResponse;
import com.careerpath.dto.response.AuthResponse;
import com.careerpath.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, and token operations")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new student account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("Registration successful", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Student login — returns JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/admin/login")
    @Operation(summary = "Admin login — returns JWT token with admin expiry")
    public ResponseEntity<ApiResponse<AuthResponse>> adminLogin(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.adminLogin(request);
        return ResponseEntity.ok(ApiResponse.ok("Admin login successful", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout — JWT is stateless; client should discard the token")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT is stateless: token invalidation is handled client-side.
        // This endpoint exists so the frontend can call it without errors.
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }
}
