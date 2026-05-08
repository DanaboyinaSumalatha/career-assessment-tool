package com.careerpath.service;

import com.careerpath.dto.request.CareerRequest;
import com.careerpath.dto.request.QuestionRequest;
import com.careerpath.dto.response.*;

import java.util.List;

public interface AdminService {

    // ── Dashboard ────────────────────────────────────────────────────────────
    AdminDashboardResponse getDashboardStats();

    // ── Students ─────────────────────────────────────────────────────────────
    List<UserResponse> getAllStudents();

    UserResponse getStudentById(Long id);

    AssessmentResultResponse getStudentResults(Long studentId);

    // ── Questions ────────────────────────────────────────────────────────────
    List<QuestionResponse> getAllQuestions(String type);

    QuestionResponse createQuestion(QuestionRequest request);

    QuestionResponse updateQuestion(Long id, QuestionRequest request);

    void deleteQuestion(Long id);

    // ── Career Paths ─────────────────────────────────────────────────────────
    List<CareerResponse> getAllCareerPaths();

    CareerResponse createCareerPath(CareerRequest request);

    CareerResponse updateCareerPath(Long id, CareerRequest request);

    void deleteCareerPath(Long id);

    // ── Analytics ────────────────────────────────────────────────────────────
    AnalyticsResponse getAnalytics();
}
