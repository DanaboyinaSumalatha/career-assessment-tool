package com.careerpath.controller;

import com.careerpath.dto.request.CareerRequest;
import com.careerpath.dto.request.QuestionRequest;
import com.careerpath.dto.response.*;
import com.careerpath.ml.MLModelState;
import com.careerpath.ml.MLModelTrainer;
import com.careerpath.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Admin", description = "Admin management endpoints")
public class AdminController {

    private final AdminService adminService;
    private final MLModelTrainer mlModelTrainer;
    private final MLModelState   mlModelState;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard statistics")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboardStats()));
    }

    // ── Students ──────────────────────────────────────────────────────────────

    @GetMapping("/students")
    @Operation(summary = "Get all registered students")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllStudents() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllStudents()));
    }

    @GetMapping("/students/{id}")
    @Operation(summary = "Get a student by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getStudentById(id)));
    }

    @GetMapping("/students/{id}/results")
    @Operation(summary = "Get a student's assessment results")
    public ResponseEntity<ApiResponse<AssessmentResultResponse>> getStudentResults(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getStudentResults(id)));
    }

    // ── Questions ─────────────────────────────────────────────────────────────

    @GetMapping("/questions")
    @Operation(summary = "List all questions (optionally filter by type: PERSONALITY, SKILLS, INTEREST)")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getAllQuestions(
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllQuestions(type)));
    }

    @PostMapping("/questions")
    @Operation(summary = "Create a new question")
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Question created", adminService.createQuestion(request)));
    }

    @PutMapping("/questions/{id}")
    @Operation(summary = "Update an existing question")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Question updated",
                adminService.updateQuestion(id, request)));
    }

    @DeleteMapping("/questions/{id}")
    @Operation(summary = "Delete a question by ID")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long id) {
        adminService.deleteQuestion(id);
        return ResponseEntity.ok(ApiResponse.ok("Question deleted"));
    }

    // ── Career Paths ──────────────────────────────────────────────────────────

    @GetMapping("/career-paths")
    @Operation(summary = "List all career paths")
    public ResponseEntity<ApiResponse<List<CareerResponse>>> getAllCareerPaths() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllCareerPaths()));
    }

    @PostMapping("/career-paths")
    @Operation(summary = "Create a new career path")
    public ResponseEntity<ApiResponse<CareerResponse>> createCareerPath(
            @Valid @RequestBody CareerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Career path created", adminService.createCareerPath(request)));
    }

    @PutMapping("/career-paths/{id}")
    @Operation(summary = "Update a career path")
    public ResponseEntity<ApiResponse<CareerResponse>> updateCareerPath(
            @PathVariable Long id,
            @Valid @RequestBody CareerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Career path updated",
                adminService.updateCareerPath(id, request)));
    }

    @DeleteMapping("/career-paths/{id}")
    @Operation(summary = "Delete a career path by ID")
    public ResponseEntity<ApiResponse<Void>> deleteCareerPath(@PathVariable Long id) {
        adminService.deleteCareerPath(id);
        return ResponseEntity.ok(ApiResponse.ok("Career path deleted"));
    }

    // ── Analytics ─────────────────────────────────────────────────────────────

    @GetMapping("/analytics")
    @Operation(summary = "Get analytics data for charts")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAnalytics()));
    }

    // ── ML Model ──────────────────────────────────────────────────────────────

    @PostMapping("/ml/retrain")
    @Operation(summary = "Trigger ML model retraining on all completed student assessments")
    public ResponseEntity<ApiResponse<MLTrainingResponse>> retrainModel() {
        MLModelTrainer.TrainingResult result = mlModelTrainer.train();

        MLTrainingResponse response = MLTrainingResponse.builder()
            .success(result.isSuccess())
            .message(result.getMessage())
            .trainingSamples(result.getTrainingSamples())
            .clusters(result.getClusters())
            .iterations(result.getIterations())
            .inertia(result.isSuccess() ? result.getInertia() : null)
            .trainedAt(result.getTrainedAt())
            .clusterSizes(result.getClusterSizes())
            .clusterLabels(result.getClusterLabels())
            .modelActive(mlModelState.isTrained())
            .build();

        return ResponseEntity.ok(ApiResponse.ok(
            result.isSuccess() ? "ML model retrained successfully" : "Training skipped",
            response));
    }

    @GetMapping("/ml/status")
    @Operation(summary = "Get current ML model status and training metadata")
    public ResponseEntity<ApiResponse<MLTrainingResponse>> getModelStatus() {
        if (!mlModelState.isTrained()) {
            MLTrainingResponse response = MLTrainingResponse.builder()
                .modelActive(false)
                .message("Model not yet trained — run POST /api/admin/ml/retrain")
                .build();
            return ResponseEntity.ok(ApiResponse.ok("ML model not trained", response));
        }

        MLModelState.ModelSnapshot snap = mlModelState.getSnapshot().get();
        MLTrainingResponse response = MLTrainingResponse.builder()
            .success(true)
            .message("ML model is active")
            .trainingSamples(snap.getTrainingDataSize())
            .clusters(snap.getKClusters())
            .iterations(snap.getIterations())
            .trainedAt(snap.getTrainedAt())
            .clusterLabels(snap.getClusterLabels())
            .modelActive(true)
            .build();

        return ResponseEntity.ok(ApiResponse.ok("ML model status", response));
    }
}
