package com.careerpath.controller;

import com.careerpath.dto.request.UpdateProfileRequest;
import com.careerpath.dto.response.ApiResponse;
import com.careerpath.dto.response.StudentDashboardResponse;
import com.careerpath.dto.response.UserResponse;
import com.careerpath.model.User;
import com.careerpath.service.AuthService;
import com.careerpath.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Student", description = "Student profile and dashboard endpoints")
public class StudentController {

    private final StudentService studentService;
    private final AuthService authService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get current student's dashboard data")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> getDashboard(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                studentService.getDashboard(currentUser.getId())));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current student's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                studentService.getProfile(currentUser.getId())));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current student's profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated",
                studentService.updateProfile(currentUser.getId(), request)));
    }
}
