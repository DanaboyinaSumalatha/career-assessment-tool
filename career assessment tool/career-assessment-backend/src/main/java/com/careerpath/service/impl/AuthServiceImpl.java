package com.careerpath.service.impl;

import com.careerpath.dto.request.LoginRequest;
import com.careerpath.dto.request.RegisterRequest;
import com.careerpath.dto.response.AuthResponse;
import com.careerpath.dto.response.UserResponse;
import com.careerpath.exception.BadRequestException;
import com.careerpath.exception.ResourceNotFoundException;
import com.careerpath.exception.UnauthorizedException;
import com.careerpath.model.Role;
import com.careerpath.model.User;
import com.careerpath.model.enums.RoleName;
import com.careerpath.repository.RoleRepository;
import com.careerpath.repository.UserRepository;
import com.careerpath.security.JwtTokenProvider;
import com.careerpath.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Value("${app.admin.email}")
    private String authorisedAdminEmail;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    // ── Register ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT role not found. Please run database seeding."));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .grade(request.getGrade())
                .city(request.getCity())
                .role(studentRole)
                .enabled(true)
                .build();

        user = userRepository.save(user);
        log.info("New student registered: {}", user.getEmail());

        String token = jwtTokenProvider.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(toUserResponse(user))
                .build();
    }

    // ── Student Login ─────────────────────────────────────────────────────────

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = authenticateAndGetUser(request);

        if (!user.getRole().getName().equals(RoleName.STUDENT)) {
            throw new UnauthorizedException("This endpoint is for students only. Please use the admin login.");
        }

        String token = jwtTokenProvider.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(toUserResponse(user))
                .build();
    }

    // ── Admin Login ──────────────────────────────────────────────────────────

    @Override
    public AuthResponse adminLogin(LoginRequest request) {
        // Only the single authorised admin email is allowed
        if (!authorisedAdminEmail.equalsIgnoreCase(request.getEmail())) {
            throw new UnauthorizedException("Access denied. This email is not authorised as admin.");
        }

        User user = authenticateAndGetUser(request);

        if (!user.getRole().getName().equals(RoleName.ADMIN)) {
            throw new UnauthorizedException("Access denied. Admin account required.");
        }

        String token = jwtTokenProvider.generateAdminToken(user);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(toUserResponse(user))
                .build();
    }

    // ── Profile ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private User authenticateAndGetUser(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        return (User) auth.getPrincipal();
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .grade(user.getGrade())
                .bio(user.getBio())
                .city(user.getCity())
                .role(user.getRole().getName().name())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .build();
    }
}
