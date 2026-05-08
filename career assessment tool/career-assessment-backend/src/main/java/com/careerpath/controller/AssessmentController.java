package com.careerpath.controller;

import com.careerpath.dto.request.AssessmentSubmitRequest;
import com.careerpath.dto.response.ApiResponse;
import com.careerpath.dto.response.AssessmentResultResponse;
import com.careerpath.dto.response.CareerRecommendationResponse;
import com.careerpath.dto.response.QuestionResponse;
import com.careerpath.model.User;
import com.careerpath.model.enums.AssessmentType;
import com.careerpath.service.AssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Assessments", description = "Questions, submission, results and recommendations")
public class AssessmentController {

    private final AssessmentService assessmentService;

    // ── Questions ─────────────────────────────────────────────────────────────

    @GetMapping("/personality/questions")
    @Operation(summary = "Get all active personality (Big Five) questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getPersonalityQuestions() {
        return ResponseEntity.ok(ApiResponse.ok(
                assessmentService.getQuestionsByType(AssessmentType.PERSONALITY)));
    }

    @GetMapping("/skills/questions")
    @Operation(summary = "Get all active skills assessment questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getSkillsQuestions() {
        return ResponseEntity.ok(ApiResponse.ok(
                assessmentService.getQuestionsByType(AssessmentType.SKILLS)));
    }

    @GetMapping("/interest/questions")
    @Operation(summary = "Get all active interest survey questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getInterestQuestions() {
        return ResponseEntity.ok(ApiResponse.ok(
                assessmentService.getQuestionsByType(AssessmentType.INTEREST)));
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    @PostMapping("/personality/submit")
    @Operation(summary = "Submit personality assessment answers")
    public ResponseEntity<ApiResponse<AssessmentResultResponse>> submitPersonality(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AssessmentSubmitRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Personality assessment completed",
                assessmentService.submitAssessment(currentUser.getId(), AssessmentType.PERSONALITY, request)));
    }

    @PostMapping("/skills/submit")
    @Operation(summary = "Submit skills assessment answers")
    public ResponseEntity<ApiResponse<AssessmentResultResponse>> submitSkills(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AssessmentSubmitRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Skills assessment completed",
                assessmentService.submitAssessment(currentUser.getId(), AssessmentType.SKILLS, request)));
    }

    @PostMapping("/interest/submit")
    @Operation(summary = "Submit interest survey answers")
    public ResponseEntity<ApiResponse<AssessmentResultResponse>> submitInterest(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AssessmentSubmitRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Interest survey completed",
                assessmentService.submitAssessment(currentUser.getId(), AssessmentType.INTEREST, request)));
    }

    // ── Results ──────────────────────────────────────────────────────────────

    @GetMapping("/results/me")
    @Operation(summary = "Get the current student's assessment results (all three types)")
    public ResponseEntity<ApiResponse<AssessmentResultResponse>> getMyResults(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                assessmentService.getMyResults(currentUser.getId())));
    }

    // ── Recommendations ───────────────────────────────────────────────────────

    @GetMapping("/recommendations")
    @Operation(summary = "Get current student's top career recommendations (requires all 3 tests done)")
    public ResponseEntity<ApiResponse<List<CareerRecommendationResponse>>> getRecommendations(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                assessmentService.getRecommendations(currentUser.getId())));
    }
}
